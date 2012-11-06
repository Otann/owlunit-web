OU = window.OU
OU.Options.DraggableItems.appendTo = undefined

window.OU.Admin = {}
Admin = window.OU.Admin

Admin.Areas =

  QuickSearch:
    selector: '#quicksearch'
    resultSelector: '#quicksearch-results'
    collection: {}
    view: {}
    defaultTimeout: 200
    timeout: null
    search: () ->
      query = $(Admin.Areas.QuickSearch.selector).find('input').val();
      if query.length >= 3
        $.get(OU.Config.url.globalSearch + query, (data) -> Admin.Areas.QuickSearch.receive(data))
      else
        $(Admin.Areas.QuickSearch.selector).find('.dropdown-menu').hide()
        Admin.Areas.QuickSearch.collection.reset();

    receive: (items) ->
      if items.length > 0
        Admin.Areas.QuickSearch.collection.reset(items)
        $(Admin.Areas.QuickSearch.selector).find('.dropdown-menu').show()
      else
        $(Admin.Areas.QuickSearch.selector).find('.dropdown-menu').html('<li class="nav-header">Nothing found</li>')

jQuery ->

  # UI effects
  $('body').delegate('.ii', 'mouseover', ->
    thing = $(this)
    if not thing.hasClass('ui-draggable')
      # make draggable
      thing.draggable(OU.Options.DraggableItems);

      # On double click add to dropbar
      thing.dblclick( ->
        ii = new OU.Ii(tag: thing)
        Admin.Areas.Dropbar.collection.add(ii)
      )
  )

  ((qs) ->

    # Init input handler
    qs.timeout = null
    $(qs.selector).find('input').keyup( ->
      clearTimeout(qs.timeout) if (qs.timeout != null)
      qs.timeout = setTimeout(qs.search, qs.defaultTimeout)
    )

    # Init backbone models
    qs.collection = new OU.IiSet()
    qs.view = new OU.IiSetView({
      collection : qs.collection,
      el: $(qs.resultSelector)[0]
    })
    qs.view.render()

  )(Admin.Areas.QuickSearch)

  $('#admin-nav').droppable(
    accept: '.ii'
    hoverClass: 'ii-hovered'
    drop: (event, ui) ->
      sample = new OU.Ii(tag: ui.draggable)
      document.location.href = '/admin' + sample.get('iiUrl')
  )

  Admin.Areas.checkCompleteAdd = () ->
    if Admin.Areas.dropFrom and Admin.Areas.dropTo
      from = Admin.Areas.dropFrom.get('iiId')
      to = Admin.Areas.dropTo.get('iiId')
      $.get('/api/drop/admin/' + from + '/' + to).success(() ->
        alert('done!')
      ).error(() ->
        alert('fail!')
      )
  Admin.Areas.dropFrom = undefined
  $('#drop-from').droppable(
    accept: '.ii'
    hoverClass: 'ii-hovered'
    drop: (event, ui) ->
      ii = new OU.Ii(tag: ui.draggable)
      view = new OU.IiView(model: ii)
      $(this).html(view.render().el)
      Admin.Areas.dropFrom = ii
      Admin.Areas.checkCompleteAdd()
  )
  Admin.Areas.dropTo = undefined
  $('#drop-to').droppable(
    accept: '.ii'
    hoverClass: 'ii-hovered'
    drop: (event, ui) ->
      ii = new OU.Ii(tag: ui.draggable)
      view = new OU.IiView(model: ii)
      $(this).html(view.render().el)
      Admin.Areas.dropTo = ii
      Admin.Areas.checkCompleteAdd()
  )
  Admin.Areas.clearDropAdd = () ->
    $('#drop-to').html()
    Admin.Areas.dropTo = undefined



#  Admin.Areas.Dropbar.collection = new OU.IiSet()
#  ((dp) ->
#
#    dp.view = new OU.IiSetView({
#      el: $(dp.selector)[0]
#      collection: dp.collection,
#    })
#
#    dp.view.render()
#
#    dp.view.$el.droppable(
#      hoverClass: 'ii-hovered',
#      accept: (draggable) ->
#        ii = new OU.Ii(tag: draggable);
#        return not dp.collection.contains(ii);
#      drop: (event, ui) ->
#        ii = new OU.Ii(tag: ui.draggable);
#        dp.collection.add(ii);
#    )
#
#  )(Admin.Areas.Dropbar)