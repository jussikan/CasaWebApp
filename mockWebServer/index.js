var express = require('express');
var app = express();
var port = 80;
var server;

server = app.listen(port, function() {
  console.log("Listening on port " + port);
});

app.get('/samplePath', function (req, res) {
  res.send("my response");
});

app.get('/mirror', function (req, res) {
  console.log(req);
  console.log(req.query);
  res.send(req.headers);
});

var homeHtml = `<!DOCTYPE html>
<html>
<head>
  <title>Mock web page title</title>
</head>
<body>

<a href="https://google.fi/">Google</a>

</body>
</html>
`;

app.get('/home', function (req, res) {
  res.send(homeHtml);
});

var desktopHtmlPage = `<!DOCTYPE html>
<html>
<head>
  <title>Mock web page title for desktop</title>
</head>
<body>
Desktop
</body>
</html>
`;

var mobileHtmlPage = `<!DOCTYPE html>
<html>
<head>
  <title>Mock web page title for mobile</title>
</head>
<body>
Mobile
</body>
</html>
`;

var mobileHtml = `<div id="deviceType">mobile</div>`;
var desktopHtml = `<div id="deviceType">desktop</div>`;

function fillHtml(filler) {
  return `<!DOCTYPE html>
<html>
<head>
  <title>General mock web page title</title>
</head>
<body>
  <a href="https://google.fi/" style="position: absolute; top: 0; left: 0; border: 1px solid red">test link</a>
  <br />
  ${filler}
  <br />
  <a href="/page/a">Go to page A</a>
  <br />
  <a href="/page/b">Go to page B</a>
  <br />
  <a href="https://www.google.fi/">Go to search engine</a>
</body>
</html>
`;
}

function pageX(argv) {
  const { name } = argv;
  return `<!DOCTYPE html>
<html>
<head>
  <title>${name}</title>
</head>
<body>
<br /><br /><br /><br />
Hello ${name}<br />
Meet my friend <a href="/page/bob">Bob</a>
</body>
</html>`;
}

var mobileUserAgentIdentifiers = ["Mobile", "Android"];

function isMobileUserAgent(userAgent) {
  return mobileUserAgentIdentifiers.some(id => userAgent.includes(id.toLowerCase()));
}

function respondPageX(req, res) {
  // const { params } = req;
  res.send(pageX(req.params));
}

function respondDesktopOrMobile(req, res) {
  if (isMobileUserAgent(req.headers['user-agent'].toLowerCase())) {
    res.send(fillHtml(mobileHtml));
    // res.send(fillHtml("mobile "+ req.headers['user-agent']));
  } else {
    res.send(fillHtml(desktopHtml));
    // res.send(fillHtml("desktop "+ req.headers['user-agent']));
  }
}

function refineRawLinksCoordinates(rawstr) {
    const xy = rawstr.split(",");
    return {
      x: xy[0],
      y: xy[1]
    };
}

const firstCharCode = 97;

function generateAllowedLinksByCoordinates(linkCoordinates) {
  return linkCoordinates.map((c, idx) => {
    const char = String.fromCharCode(firstCharCode + idx);
    // return `<a href="/page/${char}" style="position: absolute; top: ${c.y}px; left: ${c.x}px">Page ${char.toUpperCase()}</a>`;
    return formLinkAtPosition(`Page ${char.toUpperCase()}`, `/page/${char}`, c);
  });
}

function formLinkAtPosition(title, url, c) {
    return `<a href="${url}" style="position: absolute; top: ${c.y}px; left: ${c.x}px">${title}</a>`;
}

function generateDynamicPage(title, allowedLinksCoordinates, forbiddenLinksCoordinates) {
  const allowedLinks = generateAllowedLinksByCoordinates(allowedLinksCoordinates).join("<br />");
  const forbiddenLinks = forbiddenLinksCoordinates.map((c, idx) =>
    formLinkAtPosition(`Forbidden site ${idx}`, `http://forbidden.site/${idx}`, c)
  ).join("<br />");

  return `<!DOCTYPE html>
<html>
<head>
  <title>${title}</title>
</head>
<body>
${allowedLinks}
<br />
${forbiddenLinks}
</body>
</html>
`;
}

function dynamic(req, res) {
  const query = req.query;
  console.log(req.query);
  // { alink: [ '1,1', '1,20' ], flink: [ '1,30' ] }
  const alinks_raw = query.alink || [];
  const flinks_raw = query.flink || [];

  const alinks = alinks_raw.map(refineRawLinksCoordinates);
  const flinks = flinks_raw.map(refineRawLinksCoordinates);
  console.log(alinks);
  console.log(flinks);



  res.send(generateDynamicPage("Dynamic links", alinks, flinks));
}

app.get('/page/:name', respondPageX);

app.get('/desktopOrMobile', respondDesktopOrMobile);

app.get('/dynamic', dynamic);

app.get('/', respondDesktopOrMobile);
