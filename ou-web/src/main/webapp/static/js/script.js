// Bind app to loaded DOM
////////////////////////////////

$(function(){

    // UI effects
    ////////////////////////////////

    // Make items live when hovered
    $('body').delegate('.ii', 'mouseover', function(){
        var thing = $(this);
        if(!thing.hasClass('ui-draggable')) {
            // Make draggable
            thing.draggable(OU.Options.DraggableItems);

            // On double click add to dropbar
            thing.dblclick(function(){
                var ii = new OU.Ii({tag: thing});
                OU.Areas.Dropbar.collection.add(ii);
            })
        }
    });

    $('#profile').find('img').hover(
        function() {$(this).html("Go to profile")},
        function() {$(this).html("Add to profile")}
    );

    (function(qs){
        qs.timeout = null;
        $('#quicksearch').find('input').keyup(function() {
            if(qs.timeout != null) clearTimeout(qs.timeout);
            qs.timeout = setTimeout(qs.search, qs.defaultTimeout);
        });
    })(OU.Areas.QuickSearch);

    // Define and init areas
    ////////////////////////////////

    (function(dropbar){
        dropbar.collection = new OU.IiSet([
//            {id: -1, caption: 'Drag items here', url: '#'},
//            {id: -2, caption: 'Or drag from here to trash', url: '#'}
        ]);

        dropbar.view = new OU.IiSetView({
            collection : dropbar.collection,
            el : $(dropbar.selector)[0]
        });
        dropbar.view.render();

        dropbar.view.$el.droppable({
            hoverClass: 'ii-hovered',
            accept: function(draggable) {
                var ii = new OU.Ii({tag: draggable});
                return !dropbar.collection.contains(ii);
            },
            drop: function(event, ui) {
                var ii = new OU.Ii({tag: ui.draggable});
                dropbar.collection.add(ii);
            }
        });

    })(OU.Areas.Dropbar);

    (function(quicksearch){
        quicksearch.collection = new OU.IiSet();

        quicksearch.view = new OU.IiSetView({
            collection : quicksearch.collection,
            el : $(quicksearch.selector)[0]
        });

        quicksearch.view.render();
    })(OU.Areas.QuickSearch);

    $(OU.Areas.Trash.selector).droppable({
        hoverClass: 'ii-hovered',
        drop: function(event, ui) {
            var isDropbar = !!ui.draggable.closest(OU.Areas.Dropbar.selector).length;
            if (isDropbar) {
                var sample = new OU.Ii({tag: ui.draggable});
                var match = OU.Areas.Dropbar.collection.find(function(x){ return x.id == sample.id });
                ui.draggable.parent().fadeOut('normal', function(){
                    OU.Areas.Dropbar.collection.remove(match);
                });
            }

            var isProfile = true;
            if (isProfile) {
                // TODO(Anton): check if and call server callback for removal from profile
                console.log('TODO(Anton): check if and call server callback for removal from profile');
            }
        }

    });

    $(OU.Areas.Profile.selector).droppable({
        hoverClass: 'ii-hovered',
        drop: function(event, ui) {
            var sample = new OU.Ii({tag: ui.draggable});
            OU.Callbacks.handleProfileDrop(sample);
        }
    });

});