#Backbone = require './backbone'

window.OU =

  Config:
    url:
      globalSearch: '/api/search/'
      handleProfileDrop: '/api/drop/profile'

  Options:
    DraggableItems:
      helper: ->
        ii = new OU.Ii(tag: this)
        view = new OU.IiView(model: ii)
        el = view.render().el
        $(el).addClass('helper')
      zIndex: 2700
      opacity: 0.90
      cursorAt: { left: 5, top: 20 }
      distance: 5
      appendTo: 'body'
    DroppableAreas:
      accept: '.ii',
      hoverClass: 'ii-hovered'

  Callbacks:
    handleProfileDrop: (ii) ->
      console.log(ii.toJSON());
      OU.postJSON(OU.Config.url.handleProfileDrop, ii.toJSON(), (status, text) ->
        if status != 200
          alert(status + ': ' + text)
      )

  postJSON: (url, json, callback) ->
    $.ajax(
      url: url
      type: 'POST'
      headers: {'Content-Type': 'application/json'}
      data: JSON.stringify(json)
      success: (data, textStatus, jqXHR) ->
        callback(jqXHR.status, data)
      error: (jqXHR) ->
        callback(jqXHR.status, jqXHR.statusText);
    )

OU = window.OU

class OU.Ii extends Backbone.Model
  defaults:
    id:       null,
    objectid: null,
    kind:     null,
    name:     null,
    url:      null

  initialize: ->
    if @get('tag')
      tag = $(@get('tag'))

      # get data from object
      @set('id',       tag.data('objectid'));
      @set('objectId', tag.data('objectid'));
      @set('url',      tag.data('url'));
      @set('kind',     tag.data('kind'));
      @set('name',     tag.data('name'));

      # remove to be clear
      @unset('tag', {silent: true});

class OU.IiView extends Backbone.View
  tagName: 'a'
  className: 'ii'

  initialize: ->
    _.bindAll @ # bind 'this' in render to real 'this'
    @model.bind('change', @render) # call render on each change

  icon: ->
    kind = @model.get('kind')
    if kind == 'keyword'
      '<i class="icon-lemon"></i>'
    else if kind == 'movie'
      '<i class="icon-film"></i>'
    else if kind == 'person'
      '<i class="icon-user"></i>'
    else
      ''

  render: ->
    content = @model.get('name')
    $(@el)
      .attr('href',          @model.get('url'))
      .attr('data-objectid', @model.get('objectId'))
      .attr('data-url',      @model.get('url'))
      .attr('data-kind',     @model.get('kind'))
      .attr('data-name',     @model.get('name'))
#      .attr('draggable',     true) # HTML 5 not integrated yet
      .html(@icon() + content);
    this

class OU.IiSet extends Backbone.Collection
  model: OU.Ii

  initislize: ->
    _.bindAll @

  contains: (ii) ->
    found = @find( (x) -> x.id == ii.id );
    !!found

class OU.IiSetView extends Backbone.View

  initialize: ->
    _.bindAll @
    # call render on each change
    @collection.bind('add',    @render);
    @collection.bind('remove', @render);
    @collection.bind('reset',  @render);

  render: ->
    that = this
    $(@el).empty();
    @collection.each( (item) ->
      view = new OU.IiView(model: item)
      $(that.el).append($('<li>').html(view.render().el))
    )
