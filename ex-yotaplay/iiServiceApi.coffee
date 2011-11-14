jwhen = this.when

urlEncodeArgs = (args) ->
    urlencodedArgs = for own k, v of args
        k + "=" + encodeURIComponent(v.toString())
    urlencodedArgs.join("&")

iiApiUrl = "http://tytoalba.ru:8080/ex-api-1.0/flatapi/"

iiService = class iiService
    constructor: (serviceUrl) ->
        @serviceUrl = serviceUrl ? defaultApiUrl

    version: -> @query("version", {}, false)

    getSimilarMovies: (login, movieName, year, amount, showReasons) ->
        @query("getsimilarmovie",
            { login: login, movieName: movieName, year: year, amount: amount, showReasons: showReasons },
            true)

    getRecommendations: (login, amount, showReasons) ->
        @query("getrecommendations", { login: login, amount: amount, showReasons: showReasons }, true)

    getPoster: (movieName, movieYear) ->
        @query("getposter", { movieName: movieName, movieYear: movieYear }, false)

    query: (method, args, json) ->
        d = jwhen.defer()
        q = @serviceUrl + method + "?" + urlEncodeArgs(args)
        console.log(q)
        r = new XMLHttpRequest
        r.open("GET", q)
        r.onreadystatechange = () ->
            if @readyState == @DONE
                console.log(@status, @responseText)
                if @status == 200
                    d.resolve(if json then JSON.parse(@responseText) else @responseText)
                else
                    d.reject(@status)
        r.send()
        return d.promise


this.iiService = iiService
this.iiApiUrl = iiApiUrl

