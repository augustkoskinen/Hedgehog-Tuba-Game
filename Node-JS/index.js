import { WebSocketServer } from 'ws';

//vars/initialization
const wss = new WebSocketServer({ port: 8070 });

console.log("Server is running...")

//setting events
wss.on('connection', function connection(ws) {
    ws.on('error', console.error);

    ws.on('message', (e) => {
        let senddata = {
            event: 'startGame',
            seed: seed,
        }
        ws.send(JSON.stringify(senddata));
    });

    ws.on("close", () => {});
});

//makes a unique id
function newID() {
    return 'xxxxxxxx'.replace(/[xy]/g, function(c) {
        let r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}