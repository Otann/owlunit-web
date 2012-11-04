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
        $('<li class="dragging-ii">').append(view.render().el)
      zIndex: 2700
      opacity: 0.90
      cursorAt: { left: 5, top: 20 }
      distance: 5
      appendTo: '#dropbar'
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
    id:     null,
    iiId:   null,
    iiType: null,
    iiName: null,
    iiUrl:  null

  initialize: ->
    if @get('tag')
      tag = $(@get('tag'))

      # get data from object
      @set('id',     tag.data('iiId'));
      @set('iiId',   tag.data('iiId'));
      @set('iiUrl',  tag.data('iiUrl'));
      @set('iiType', tag.data('iiType'));
      @set('iiName', tag.data('iiName'));

      # remove to be clear
      @unset('tag', {silent: true});

class OU.IiView extends Backbone.View
  tagName: 'a'
  className: 'ii'

  initialize: ->
    _.bindAll @ # bind 'this' in render to real 'this'
    @model.bind('change', @render) # call render on each change

  render: ->
    content = @model.get('iiName')
    type = @model.get('iiType')
    if type == 'keyword'
      content = '<i class="icon-tag icon-white"></i> ' + content
    $(@el)
      .attr('draggable',    true)
      .attr('href',         @model.get('iiUrl'))
      .attr('data-ii-id',   @model.get('iiId'))
      .attr('data-ii-url',  @model.get('iiUrl'))
      .attr('data-ii-type', @model.get('iiType'))
      .attr('data-ii-name', @model.get('iiName'))
      .html(content);
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
