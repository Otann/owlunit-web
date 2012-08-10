/*
    Author: Anton Chebotaev
*/

(function(){

    // Owl Unit namespace
    ////////////////////////////////

    window.OU = {};

    // Define Application singleton
    ////////////////////////////////

    OU.Options = {
        DraggableItems: {
            helper: 'clone',
            opacity: 0.65,
            zIndex: 2700
        }
    };

    // Define classes
    ////////////////////////////////

    OU.Ii = Backbone.Model.extend({
        defaults: {
            id: -1,
            caption: 'NO TITLE'
        },
        renderSpan: function() {
            var text = $(document.createElement('span'))
                .attr('data-itemid', this.get('id'))
                .addClass('ii')
                .html(this.get('caption'));
            return text;//.draggable(OU.Options.DraggableItems);
        },
        renderLi: function() {
            return $(document.createElement('li')).html(this.renderSpan());
        }
    });

    OU.IiSet = Backbone.Collection.extend({
        model: OU.Ii
    });

    window.test = new OU.Ii({
        id: 239,
        caption: 'FML'
    });





//    App = {};
//    App.makeIi = function(caption, iiid){
//        var text = $(document.createElement('span'))
//            .attr('data-itemid', iiid)
//            .attr('data-caption', caption)
//            .addClass('ii ui-draggable')
//            .html(caption);
//        var element = $(document.createElement('li')).html(text);
//        element.draggable(App.options.ii);
//        return element;
//    };
//
//    App.area = App.area || {};
//    App.area.trash = $('.trashbin');
//    App.area.profile = $('.profile');
//

})();

$(function(){

    for (var i = 1; i <= 100000; i++) {
        var item = new OU.Ii({
            id: i,
            caption: 'Item ' + i
        });
        $('#quicksearch-results').append(item.renderLi());
    }

    // Make items draggable when hovered
    $('body').delegate('.ii', 'mouseover', function(){
        var thing = $(this);
        if(!thing.hasClass('ui-draggable')) {
            thing.draggable(OU.Options.DraggableItems);
        }
    });

//    // Make items draggable when hovered
//    $('body').delegate('.ii', 'mouseover', function(){
//        var thing = $(this);
//        if(!thing.hasClass('ui-draggable')) {
//            thing.draggable(App.options.ii);
//        }
//    });
//
//    // Make areas droppable
//    App.area.trash.droppable({
//        drop: function(event, ui) {
//            console.log(ui.draggable.data('itemid')  + 'should be trashed');
//            ui.draggable.parent().fadeOut();
//        },
//        hoverClass: 'ii-hovered'
//    });
//
//    App.area.profile.droppable({
//        accept: '.ii',
//        drop: function(event, ui) {
//            console.log(ui.draggable.data('itemid') + 'should be profiled');
//        },
//        hoverClass: 'ii-hovered'
//    });

});



