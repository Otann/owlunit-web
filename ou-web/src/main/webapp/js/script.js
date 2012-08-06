/*
    Author: Anton Chebotaev
*/

$(function(){

    /** @namespace window.OUApp */
    window.App = {};

    App.makeIi = function(caption, iiid){
        var text = $(document.createElement('span'))
            .attr('data-itemid', iiid)
            .attr('data-caption', caption)
            .addClass('ii ui-draggable')
            .html(caption);
        var element = $(document.createElement('li')).html(text);
        element.draggable(App.options.ii);
        return element;
    };

    App.options = App.options || {};
    App.options.ii = {
        helper: 'clone',
        opacity: 0.65,
        zIndex: 2700 };

    App.area = App.area || {};
    App.area.trash = $('.trashbin');
    App.area.profile = $('.profile');

    // Make items draggable when hovered
    $('body').delegate('.ii', 'mouseover', function(){
        var thing = $(this);
        if(!thing.hasClass('ui-draggable')) {
            thing.draggable(App.options.ii);
        }
    });

    // Make areas droppable
    App.area.trash.droppable({
        drop: function(event, ui) {
            console.log(ui.draggable.data('itemid')  + 'should be trashed');
            ui.draggable.parent().fadeOut();
        },
        hoverClass: 'ii-hovered'
    });

    App.area.profile.droppable({
        accept: '.ii',
        drop: function(event, ui) {
            console.log(ui.draggable.data('itemid') + 'should be profiled');
        },
        hoverClass: 'ii-hovered'
    });

});





