movieView = YVWC.Engine.views.movie
if !movieView? or !movieView.bindDetails? then return

hookMethod = (obj, method, before, after) ->
    old = obj[method]
    obj[method] = () ->
        before.apply(this, arguments) if before
        old.apply(this, arguments)
        after.apply(this, arguments) if after
    obj[method].hookPrev = old

unhookMethod = (obj, method) ->
    if !obj[method].hookPrev? then return
    obj[method] = obj[method].hookPrev


iiMovieLoadedEvent = document.createEvent("Event")
iiMovieLoadedEvent.initEvent("iiMovieLoaded", false, false)

onMovieLoaded = () ->
    document.dispatchEvent(iiMovieLoadedEvent)

hookMethod(movieView, "bindDetails", null, onMovieLoaded)
if YVWC.Engine.currentView == movieView
    onMovieLoaded()
