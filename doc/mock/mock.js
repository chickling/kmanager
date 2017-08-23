let http = require('http');
let port = 8099;
let har = require('./har.json');
let offsetData = require('./offset.json');
offsetData = JSON.stringify(offsetData);
let data = har.log.entries;

function removeQuerystring(url){
    if(url.indexOf('?')>0){
        url  = url.substring(0, url.indexOf('?'));
    }
    return url;
}
http.createServer((req, res) => {
    console.log(req.url);
    
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'POST, GET, OPTIONS');
    res.setHeader('Content-Type', 'application/json');

    var reg = /\/group\/(\S+)\/(\S+)/g;

    if(req.url.match(reg)){
        return res.end(offsetData);
    }

    var item = data.find(item => {
        reg = /\/group\/(\S+)/g;
        
        if (req.url.match(reg)) {
            if (item.request.url.match(reg)) {
                return item;
            }
        }
        reg = /\/stats\/topic\/(\S+)/g;
        if (req.url.match(reg)) {
            if (item.request.url.match(reg)) {
                return item;
            }
        }
        console.log(removeQuerystring(item.request.url) , removeQuerystring(`http://mockuphost${req.url}`))
        if (removeQuerystring(item.request.url) == removeQuerystring(`http://mockuphost${req.url}`)) {
            return item;
        }
    });
    if (item) {
        res.end(item.response.content.text);
    }
    res.end('{}');

}).listen(port, (err) => {
    if (err) {
        console.log(err);
    }
    console.log(`service is running at ${port}`);
});

data.forEach(item => {
    if (item.request.url.indexOf('http://mockhost/') >= 0) {
        console.log(item.request.method,
            item.request.url
            //,item.response.content.text
        )
    }
})

