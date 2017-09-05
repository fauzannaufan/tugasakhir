var req;
var isIE;
var kueri;
var sid;

function init() {
    kueri = getCookie("kueri");
    sid = getCookie("sid");
}

function sendtoDB(id,status) {
    //var url = "http://localhost:8080/SearchHadis_Service/setRelevant?kueri="+kueri+"&sid="+sid+"&id="+id+"&status="+status+"";
    var url = "http://167.205.35.177:8080/SearchHadis_Service/setRelevant?kueri="+kueri+"&sid="+sid+"&id="+id+"&status="+status+"";
    req = initRequest();
    req.open("GET", url, true);
    req.onreadystatechange = callback;
    req.send(null);
    req.setRequestHeader("Access-Control-Allow-Origin", "*");
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function initRequest() {
    if (window.XMLHttpRequest) {
        if (navigator.userAgent.indexOf('MSIE') !== -1) {
            isIE = true;
        }
        return new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        isIE = true;
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
}

function vote_up(key) {
    if (document.getElementById("up-"+key).src.indexOf("up.png") !== -1) {
        document.getElementById("up-"+key).src = "up0.png";
        sendtoDB(key,"uR");
    } else {
        document.getElementById("up-"+key).src = "up.png";
        document.getElementById("down-"+key).src = "down0.png";
        sendtoDB(key,"sR");
    }
}

function vote_down(key) {
    if (document.getElementById("down-"+key).src.indexOf("down.png") !== -1) {
        document.getElementById("down-"+key).src = "down0.png";
        sendtoDB(key,"uNR");
    } else {
        document.getElementById("down-"+key).src = "down.png";
        document.getElementById("up-"+key).src = "up0.png";
        sendtoDB(key,"sNR");
    }
}

function callback() {
    if(req.readyState === 4) {
        if (req.status === 200) {
            //Sukses
        }
    }
}