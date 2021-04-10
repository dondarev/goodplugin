var canvas = document.getElementById('canvas');
var botton = document.getElementById("sendRequest");
var request = new XMLHttpRequest();
botton.style.display = "none"
var width = canvas.width;
var height = canvas.height;
var ctx = canvas.getContext('2d');
let flagDraw = false;
var beginRectX =0;
var endRectX=0;
canvas.onmousedown = startDrawing;
canvas.onmouseup = closeDraw;
canvas.onmousemove = drawRect;
canvas.onmouseout = closeDraw2;

function drawRect(e) {
    if (flagDraw) {
        clearCanvas();
        ctx.lineWidth = 3;
        ctx.rect(beginRectX, 0, e.offsetX -beginRectX, height - 18);
        ctx.stroke();
    }
}

function closeDraw(e) {
    flagDraw = false;
    ctx.closePath();
    endRectX = e.offsetX;
    botton.style.display="block";
}

function closeDraw2(e) {
    flagDraw = false;
}
function startDrawing(e) {
    beginRectX = e.offsetX;
    ctx.beginPath();
    flagDraw = true;
}

function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    draw();
}

function draw() {
    if (canvas.getContext) {
        ctx.fillStyle = "orange";
        ctx.strokeStyle = "grey";
        ctx.lineWidth = 5;
        drawScale();
        ctx.beginPath()
        ctx.fillRect(0, 0, canvas.width, canvas.height - 18);
        ctx.rect(2, 2, canvas.width - 5, canvas.height - 18);
        ctx.strokeStyle = "grey";
        ctx.lineWidth = 5;
        ctx.stroke();
        ctx.fill();
        ctx.closePath()
    }
}

function drawScale() {
    ctx.beginPath();
    ctx.lineWidth = 2;
    for (var i = 0; i < 73; i++) {
        ctx.moveTo(i * 10, height - 17);
        ctx.lineTo(i * 10, height - 11);
    }
    ctx.stroke();
    ctx.closePath();
}

function getDataFromPeriod(){
    var dateBeginRect = new Date(2020,0,1);
    var dateBegin = new Date(dateBeginRect.getTime()+(beginRectX*24*60*60*1000));
    var dateEnd = new Date(dateBeginRect.getTime()+(endRectX*24*60*60*1000));
    var masDate={};
    masDate.begin = dateBegin.getTime();
    masDate.end = dateEnd.getTime();
    return masDate;
}

botton.onclick = function (){
    var dataFromPeriod = getDataFromPeriod();
    sednRequest(dataFromPeriod.begin,dataFromPeriod.end)

}

function sednRequest(dateOne,dateTwo) {
    var url = "http://localhost:2990/jira/rest/helloworld/1.0/RunChart?dateOne="+dateOne+"&dateTwo="+dateTwo
    request.open("GET", url);
    request.send();
    setTimeout(location.reload(), 3000);
}


setTimeout(draw(), 10);