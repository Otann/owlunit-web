# react on movie load (hook script will dispatch event)
document.addEventListener("iiMovieLoaded", (->onMovieLoaded()), false)

# run hook script in page context
hookUrl = chrome.extension.getURL("iiPlayHook.js")

scriptNode = document.createElement("script")
scriptNode.setAttribute("src", hookUrl)
scriptNode.setAttribute("type", "text/javascript")

headNode = document.getElementsByTagName("head")[0]
headNode.appendChild(scriptNode)

############################################################

service = new iiService(iiApiUrl)
shownMoviesLimit = 3

reportError = (x) -> console.log("ii service error", x)

showSimilarMovies = (similar) ->
    parse = /^Movie\(([0-9a-f\-]+),([^,]+),(\d+),([^)]+)\)$/
    movies = []
    for own movie, weight of similar
        p = parse.exec(movie)
        if p?
            movies.push { uuid: p[1], name: p[2], year: p[3], description: p[4], weight: weight }
    movies = movies[0...shownMoviesLimit]

    # render panel
    renderSimilar movies

    # load posters
    for movie in movies
        do (movie) ->
            posterQ = service.getPoster(movie.name, movie.year)
            posterQ.then(((posterUrl) -> showPoster(movie, posterUrl)), reportError)

showPoster = (movie, posterUrl) ->
    movieItem = document.getElementById("iiMovieItem"+movie.uuid)
    if !movieItem? then return
    movieItem.outerHTML = applyMovieTemplate(movie, posterUrl)

renderSimilar = (movies) ->
    moviesHtml = movies.map(applyMovieTemplate)
    similarBody = applySimilarTemplate(moviesHtml)
    panelHtml = applyPanelTemplate(similarBody, "Похожие фильмы")
    sidebarContainer = document.querySelector("#content .movie-card .content-sup")
    sidebarContainer.appendChild(el = document.createElement("div"))
    el.innerHTML = panelHtml

onMovieLoaded = () ->
    movieName = document.querySelector("#content .movie-card h2.page-title .translation")?.innerText
    movieName ?= document.querySelector("#content .movie-card h2.page-title")?.innerText
    if !movieName? then return
    metaTable = document.querySelector("#content .movie-card .content-main table.meta-list")
    if !metaTable? then return
    movieYear = document.evaluate(".//tr[starts-with(td[1],'Год')]/td[normalize-space(@class)='meta-info']",\
        metaTable, null, XPathResult.STRING_TYPE, null).stringValue

    similarQ = service.getSimilarMovies("", movieName, movieYear, shownMoviesLimit, false)
    similarQ.then(showSimilarMovies, reportError)

applyPanelTemplate = (body, title) ->
    "<div class=\"panel\"><header class=\"clear\"><h3>#{title}</h3></header>"+
        "<div class=\"content\">#{body}</div></div>"
applySimilarTemplate = (list) ->
    '<ul style="list-style: none; margin: 0;">' + (list.join("")) + '</ul>'
applyMovieTemplate = (movie, posterUrl) ->
    if posterUrl?
        imageCode = "<figure class=\"poster\"><img src=\"#{posterUrl}\" width=\"130\"/></figure>"
    else imageCode = ""
    "<li id=\"iiMovieItem#{movie.uuid}\" style=\"display: inline-block; width:130px; vertical-align: top; padding: 0 0 0 5px;\">"+
        "<a href=\"http://yotaplay.ru/search/#{movie.name}\">"+
        "<b>#{movie.name}</b></a>"+imageCode+"</li>\n"
