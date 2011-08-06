/* Author: Anton Chebotaev anton.chebotaev@gmail.com

*/

$(document).ready(function(){
    var draggable_properties ={
                helper : 'clone',
                opacity : 0.8,
                zIndex: 999,
                cursor: 'crosshair'
            };

    $('div.ii-tag').draggable(draggable_properties);
    $('div.ii-recommendation').draggable(draggable_properties);
    $('a.reason').draggable(draggable_properties);

    var make_reason = function(tag) {
        if (tag.hasClass('reason')) {
            return tag;
        } else {
            var name = $('.name', tag).html();
            return $('<a href="#" class="recommender-arg"></a>').html(name);
        }
    };

    $('div#recommender-box').droppable({
                hoverClass: 'drag-hover',
                tolerance : 'pointer',
                accept : 'div.ii-tag, a.reason, div.ii-recommendation',
                drop : function(event, ui) {

                    if ($('.recommended-box-item').length == 0) {
                        $('#recommender-box-helper').hide();
                    }

                    var tag = ui.draggable.clone();
                    tag.draggable(draggable_properties);
                    tag.addClass('recommended-box-item');

                    if (tag.hasClass('ii-tag')) {
                        $('.weight', tag).remove();
                    }

                    if (tag.hasClass('ii-recommendation')) {
                        $('.ii-weight', tag).remove();
                        $('.ii-reasons', tag).remove();
                    }

                    $('#recommender-items').append(tag);
                }
            });

    $('#trash-box').click(function() {
        log('clicked');
    });

    $('#trash-box').droppable({
                hoverClass: 'drag-hover',
                tolerance : 'pointer',
                accept : '.recommended-box-item',
                drop : function(event, ui) {
                    ui.draggable.remove();
                    log();
                    $('#recommender-items');
                    if ($('.recommended-box-item').length == 1) {
                        $('#recommender-box-helper').show();
                    }
                }
            });

});





















