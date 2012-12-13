
OU = window.OU

OU.Areas =
  Dropbar:
    selector: '#dropbar',
    collection: {},
    view: {}
  QuickSearch:
    selector: '#quicksearch-results'
    collection: {}
    view: {}
    defaultTimeout: 200
    timeout: null
    search: () ->
      area = $('#quicksearch')
      area.find('.hint').html('');
      query = area.find('input').val();
      if query.length >= 3
        $.get(OU.Config.url.globalSearch + query, (data) -> OU.Areas.QuickSearch.receive(data))
      else
        OU.Areas.QuickSearch.collection.reset();

    receive: (items) ->
      hint = $('#quicksearch').find('.hint');
      if items.length > 0
        hint.html('')
      else
        hint.html('Nothing found, try another letters')
      OU.Areas.QuickSearch.collection.reset(items);


  Trash:
    selector: "#trashbin"
  Profile:
    selector: "#profile"

jQuery ->

  # align profile image
  sidebar_img = $('.sidebar-pic img')
  profile_img = $('.picture-image')
  i = new Image();
  i.onload = ->
    if this.height > this.width
      sidebar_img.css("max-width", "40px")
      profile_img.css("max-width", "150px")
    else
      sidebar_img.css("max-height", "40px")
      profile_img.css("max-height", "150px")
  i.src = sidebar_img.attr('src');

  # UI effects
  $('body').delegate('.ii', 'mouseover', ->
    thing = $(this)
    if not thing.hasClass('ui-draggable')
      # make draggable
      thing.draggable(OU.Options.DraggableItems);

      # On double click add to dropbar
      thing.dblclick( ->
        ii = new OU.Ii(tag: thing)
        OU.Areas.Dropbar.collection.add(ii)
      )
  )

#  $('#profile').find('img').hover(
#    () -> $(this).html("Go to profile"),
#    () -> $(this).html("Add to profile")
#  )

  ((qs) ->

    # Init input handler
    qs.timeout = null
    $('#quicksearch').find('input').keyup( ->
      clearTimeout(qs.timeout) if (qs.timeout != null)
      qs.timeout = setTimeout(qs.search, qs.defaultTimeout)
    )

    # Init backbone models
    qs.collection = new OU.IiSet();
    qs.view = new OU.IiSetView({
      collection : qs.collection,
      el: $(qs.selector)[0]
    });
    qs.view.render();

  )(OU.Areas.QuickSearch)

  OU.Areas.Dropbar.collection = new OU.IiSet()
  ((dp) ->

    $(dp.selector).find('.ii').map((i, t) ->
      dp.collection.add(new OU.Ii(tag: t))
    )
    dp.view = new OU.IiSetView({
      el: $(dp.selector)[0]
      collection: dp.collection,
    })

    dp.view.render()

    dp.view.$el.droppable(
      accept: '.ii'
      activeClass: 'ii-active'
      hoverClass: 'ii-hovered',
      accept: (draggable) ->
        ii = new OU.Ii(tag: draggable);
        return not dp.collection.contains(ii);
      drop: (event, ui) ->
        ii = new OU.Ii(tag: ui.draggable);
        dp.collection.add(ii);
    )

  )(OU.Areas.Dropbar)

  $(OU.Areas.Profile.selector).droppable(
    accept: '.ii'
    activeClass: 'ii-active'
    hoverClass: 'ii-hovered'
    drop: (event, ui) ->
      sample = new OU.Ii(tag: ui.draggable)
      OU.Callbacks.handleProfileDrop(sample)
  )

  $(OU.Areas.Trash.selector).droppable(
    accept: '.ii'
    activeClass: 'ii-active'
    hoverClass: 'ii-hovered',
    drop: (event, ui) ->
      isFromDropbar = !!ui.draggable.closest(OU.Areas.Dropbar.selector).length
      if isFromDropbar
        sample = new OU.Ii(tag: ui.draggable)
        match = OU.Areas.Dropbar.collection.find( (x) -> x.id == sample.id )
        ui.draggable.parent().fadeOut('normal', () -> OU.Areas.Dropbar.collection.remove(match))

      isFromProfile = false
      if isFromProfile
        #TODO(Anton): implement trash-profile drop
        console.log('Check if and call server callback for removal from profile');
  )

  $('#apply').click(->
    itemIds = OU.Areas.Dropbar.collection.models.map((x) -> x.get('objectId'))
    parameter = itemIds.reduce((a, b) -> a + '+' + b)
    document.location.href = '/recommendations?query=' + parameter;
  )