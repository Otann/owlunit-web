// Init app

(function(){

    // Owl Unit namespace and application singletons
    ////////////////////////////////

    window.OU = {
        Options: {
            DraggableItems: {
                helper: function(event){
                    var ii = new OU.Ii({tag: this});
                    var view = new OU.IiView({model: ii});
                    return $('<li>').append(view.render().el);
                },
                appendTo: '#dropbar',
                opacity: 0.90,
                cursorAt: { left: 5, top: 20 },
                distance: 5,
                zIndex: 2700
            },
            DroppableAreas: {
                accept: '.ii',
                hoverClass: 'ii-hovered'
            }
        },
        Areas: {
            Dropbar: {
                selector: '#dropbar',
                collection: {},
                view: {}
            },
            QuickSearch: {
                selector: '#quicksearch-results',
                collection: {},
                view: {}
            },
            Trash: {
                selector: '#trashbin'
            },
            Profile: {
                selector: '#profile'
            }
        },
        Callbacks: {
            test: function(params) { console.log('Test success!', params); },
            receiveSearchedIi: function(items){
                // look for IiTag in scala
                // items = [{id: 'mongo_id', caption: 'Toy Story', url: '#'}, ...]
                OU.Areas.QuickSearch.collection.reset(items);
            }
        }
    };

    // Define classes
    ////////////////////////////////

    OU.Ii = Backbone.Model.extend({
        initialize: function(options){
            if (this.get('tag')) {
                var tag = $(this.get('tag'));
                this.set('id', tag.data('id'));
                this.set('url', tag.attr('href'));
                this.set('caption', tag.data('caption'));
                this.unset('tag', {silent: true});
            }
        },
        defaults: {
            id: null,
            caption: null
        }
    });

    OU.IiView = Backbone.View.extend({
        tagName: 'a',
        className: 'ii',
        initialize : function(options) {
            this.render = _.bind(this.render, this); // bind 'this' in render to real 'this'
            this.model.bind('change', this.render); // call render on each change
        },
        render: function() {
            $(this.el)
                .attr('href', this.model.get('url'))
                .attr('data-id', this.model.get('id'))
                .attr('data-caption', this.model.get('caption'))
                .html(this.model.get('caption'));
            return this;
        }
    });

    OU.IiSet = Backbone.Collection.extend({
        model: OU.Ii,
        initialize: function(){
            this.contains = _.bind(this.contains, this); // bind 'this' in contains to real 'this'
        },
        contains: function(ii){
            var found = this.find( function(x){ return x.id == ii.id });
            return !!found;
        }
    });

    OU.IiSetView = Backbone.View.extend({
        initialize: function() {
            this.render = _.bind(this.render, this);     // bind 'this' into render to real 'this'
            this.collection.bind('add', this.render);    // call render on each change
            this.collection.bind('remove', this.render); // call render on each change
            this.collection.bind('reset', this.render);  // call render on each change
        },
        render: function() {
            var that = this;
            // Clear out this element.
            $(this.el).empty();

            this.collection.each(function(item) {
                var view = new OU.IiView({model: item});
                $(that.el).append($('<li>').html(view.render().el));
            });
        }
    });

})();

// Bind app to loaded DOM

$(function(){

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

    // Define and init areas
    ////////////////////////////////

    (function(dropbar){
        dropbar.collection = new OU.IiSet([
            {id: -1, caption: 'Drag items here', url: '#'},
            {id: -2, caption: 'Or drag from here to trash', url: '#'}
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
            // TODO(Anton): call server callback for adding to profile
            console.log('TODO(Anton): call server callback for adding to profile');
        }
    });

});