import { WebSocketServer } from 'ws';

//vars/initialization
const wss = new WebSocketServer({ port: 8090 });
let playerlist = [];
let start = false;
let startedgame = false;
let seed = Math.floor(Math.random() * 10000)

console.log("Server is running...")

//setting events
wss.on('connection', function connection(ws) {
    if(playerlist<1) startedgame = false;
    console.log("Player Connected!");
    ws.on('error', console.error);
    const player = new Player(ws, 0, 0);
    let playeri = playerlist.length;
    let id = player.id

    ws.send(JSON.stringify({
        event: 'setWS',
        id : id,
        playerlist : getSendable(playerlist)
    }));

    playerlist.push(player);

    for(let i = 0; i<playerlist.length;i++) {
        if(i!=playeri&&!playerlist[i].ingame)
            playerlist[i].ws.send(JSON.stringify({
                event: 'joinPlayer',
                player : player.getSendable()
            }));
    }

    ws.on('message', (e) => {
        const data = JSON.parse(e);
        playeri = findPlayer(id);
        switch (data.event) {
            case "readyPlayer" : {
                playerlist[playeri].ready = true;
                start = playerlist.length>1;
                for(let i = 0; i < playerlist.length; i++)
                    if(!playerlist[i].ready&&!playerlist[i].ingame)
                        start = false;

                if(start&&!startedgame) {
                    let maptype = Math.floor(Math.random() * 2);
                    for(let i = 0; i < playerlist.length; i++) {
                        playerlist[i].ws.send(JSON.stringify({
                            event: 'startGame',
                            maptype: maptype
                        }));
                        playerlist[i].ingame = true;
                        startedgame = true;
                    }
                }

                for(let i = 0; i < playerlist.length; i++)
                    if(i!=playeri)
                        playerlist[i].ws.send(JSON.stringify({
                            event : "updateOtherReady",
                            id : id,
                            ready : true
                        }));
                break;
            }
            case "updateSkin" : {
                playerlist[playeri].skin = parseInt(data.skin);
                for(let i = 0; i < playerlist.length; i++)
                    if(i!=playeri&&!playerlist[i].ingame)
                        playerlist[i].ws.send(JSON.stringify({
                            event : "updateOtherSkin",
                            id : id,
                            skin : parseInt(data.skin)
                        }));
                break;
            }
            case "unReadyPlayer" : {
                playerlist[playeri].ready = false;
                for(let i = 0; i < playerlist.length; i++)
                    if(i!=playeri&&!playerlist[i].ingame)
                        playerlist[i].ws.send(JSON.stringify({
                            event : "updateOtherReady",
                            id : id,
                            ready : false
                        }));
                break;
            }
            case "joinedPlayer" : {
                playerlist[playeri].joined = true;
                for(let i = 0; i < playerlist.length; i++)
                    if(i!=playeri&&!playerlist[i].ingame)
                        playerlist[i].ws.send(JSON.stringify({
                            event : "updateOtherJoined",
                            id : id,
                            joined : true
                        }));
                break;
            }
            case "updatePos" : {
                playerlist[playeri].x = parseFloat(data.x);
                playerlist[playeri].y = parseFloat(data.y);
                playerlist[playeri].eyerot = parseFloat(data.eyerot);
                playerlist[playeri].bodyrot = parseFloat(data.bodyrot);
                playerlist[playeri].justshot = data.justshot=='true';
                let playerdead = data.dead&&playerlist[playeri].dead;
                playerlist[playeri].dead = data.dead=='true';
                let playerjumped = data.jumped&&playerlist[playeri].jumped;
                playerlist[playeri].jumped = data.jumped=='true';
                let senddata = {
                    event : "updateOtherPos",
                    player : player.getSendable(),
                    deadcloud : playerdead,
                    jumpcloud : playerjumped
                }
                for(let i = 0; i < playerlist.length; i++)
                    if(i!=playeri)
                        playerlist[i].ws.send(JSON.stringify(senddata));
                break;
            }
        }
    });

    ws.on("close", () => {
        console.log("Player Disconnected!");
        playeri = findPlayer(id);
        for(let i = 0; i < playerlist.length; i++)
            if(i!=playeri)
                playerlist[i].ws.send(JSON.stringify({
                    event: 'leavePlayer',
                    player : player.getSendable()
                }));
        playerlist.splice(playeri,1);
    });
});

function findPlayer(ws) {
    for(let i = 0; i<playerlist.length;i++)
        if(playerlist[i].id === ws)
            return i;
    return -1;
}

class Player {
    ws = null;
    id = '';
    x = 0;
    y = 0;
    bodyrot = 0;
    eyerot = 0;
    dead = false;
    justshot = false;
    ingame = false;
    jumped = false;
    skin = 1;
    ready = false;
    joined = false;
    name = '';
    constructor(ws, x, y) {
        this.ws = ws;
        this.x = x;
        this.y = y;
        this.name = 'Player ' + (playerlist.length+1);
        this.id = newID();
    }
    setWS(ws) {
        this.ws = ws;
    }
    getSendable(){
        return {
            x: this.x,
            y: this.y,
            name: this.name,
            dead: this.dead,
            bodyrot: this.bodyrot,
            eyerot: this.eyerot,
            justshot: this.justshot,
            ingame: this.ingame,
            id: this.id,
            skin: this.skin,
            jumped: this.jumped,
            ready: this.ready,
            joined: this.joined
        };
    }
}

function getSendable(list) {
    let retlist = [];
    for(let i = 0; i<list.length;i++) {
        retlist.push(list[i].getSendable());
    }
    return retlist;
}


//makes a unique id
function newID() {
    return 'xxxxxxxx'.replace(/[xy]/g, function(c) {
        let r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}