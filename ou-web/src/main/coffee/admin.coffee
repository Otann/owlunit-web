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
        $(Admin.Areas.QuickSearch.selector).find('.dropdown-menu').show()
        $.get(OU.Config.url.globalSearch + query, (data) -> Admin.Areas.QuickSearch.receive(data))
      else
        $(Admin.Areas.QuickSearch.selector).find('.dropdown-menu').hide()
        Admin.Areas.QuickSearch.collection.reset();

    receive: (items) ->
      if items.length > 0
        Admin.Areas.QuickSearch.collection.reset(items)
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