//scroll the page whenever needed
function scrollToPlace(place) {
    $('html,body').animate(
        {
            scrollTop: place.offset().top + 1
        },
        {
            duration: 500,
            easing: "easeInOutQuart"
        }
    );
}

function eventHandlers() {
    $('.nav a').live('click tap', function() {
        var destination = $(this).attr('href');
        scrollToPlace($(destination));
        return false;

    })
}

/**
 * Processes message to page contest
 * @param text message content
 * @param type highlighting type
 */
function addMessage(text, type) {
    var content = '<a class="close" href="#">×</a><p>' + text + '</p>';
    var msg = $('<div>')
        .addClass('alert-message fade in')
        .html(content);
    if (type) {
        msg.addClass(type);
    }
    if (type === 'info') {
        setTimeout(function(){ msg.fadeOut(500); }, 1000);
    }
    msg.alert();
    $('#msg').append(msg);
}

function hideItems() {
    $('#items').hide();
}

function loadItems(items) {
    $('#items').show();

    if ($.isEmptyObject(items)) {
        $('#items-empty').show();
        $('#items-content').hide();
        addMessage('No items was found', 'info');
    } else {
        $('#items-content').show();
        $('#items-empty').hide();
        var table = $('#items-body');
        table.html('');

        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var metaContent = '';
            for (key in item.meta) {
                metaContent += '<strong>' + key + ': </strong>' + item.meta[key]  + '<br/>';
            }
            var a = $('<th>').append(createClickableUUID(item.id));
            var meta = '<td>' + metaContent + '</td>';
            var row = $('<tr>').append(a).append(meta);
            table.append(row);
        }

        var pluralized = items.length % 2 == 1 ? ' item' : ' items';
        addMessage('Loaded ' + items.length + pluralized, 'info');
    }

}

function hideItem() {
    $('#item-meta').hide();
    $('#item-components').hide();
}

function loadItem(item) {
    addMessage('Loaded item with id = <strong>' + item.id + '</strong>', 'info');
    $('#loadByUUIDForm_uuid').val(item.id);
    $('#updateMetaForm_uuid').val(item.id);
    $('#deleteForm_uuid').val(item.id);
    $('#updateComponentForm_item').val(item.id);

    reloadMeta(item);
    reloadComponents(item);

    console.log(item);
}

function reloadMeta(item) {

    $('#item-meta').show();
    if (item.meta != null) {
        if ($.isEmptyObject(item.meta)) {
            $('#item-meta-empty').show();
            $('#item-meta-null').hide();
            $('#item-meta-content').hide();
        } else {
            $('#item-meta-null').hide();
            $('#item-meta-empty').hide();
            $('#item-meta-content').show();
            var table = $('#item-meta-body');
            table.html('');

            for (key in item.meta) {
                var content = '<th>' + key + '</th><td>' + item.meta[key] + '</td>';
                var row = $('<tr>').html(content);
                table.append(row);
            }
        }
    } else {
        $('#item-meta-null').show();
        $('#item-meta-content').hide();
        $('#item-meta-empty').hide();
    }

}

function reloadComponents(item) {

    $('#item-components').show();
    if (item.components != null) {
        if ($.isEmptyObject(item.components)) {
            $('#item-components-empty').show();
            $('#item-components-null').hide();
            $('#item-components-content').hide();
        } else {
            $('#item-components-null').hide();
            $('#item-components-empty').hide();
            $('#item-components-content').show();
            var table = $('#item-components-body');
            table.html('');

            for (id in item.components) {
                var component = $('<th>').append(createClickableUUID(id));
                var value = '<td>' + item.components[id] + '</td>';
                var row = $('<tr>').append(component).append(value);
                table.append(row);
            }
        }
    } else {
        $('#item-components-content').hide();
        $('#item-components-empty').hide();
        $('#item-components-null').show();
    }

}

/**
 * Processes AJAX result from page
 * @param response raw string from server
 * @param target target object to be replaced
 * @param noUpdate don't load item data to page components
 */
function processResult(response, target, noUpdate) {
    if (target != null && response.html != null) {
        target.replaceWith(response.html);
    }
    if (response.data != null && !noUpdate) {
        var data = JSON.parse(response.data);
        $('#results-intro').hide();
        if (data.id != null) {
            hideItems();
            loadItem(data);
        } else {
            hideItem();
            loadItems(data);
        }

    }
    if (response.text != null) {
        addMessage(response.text, response.type);
    }
}

/**
 * Processes AJAX result for forms
 * @param form target form
 * @param event event from callback
 */
function processForm(form, event, noLoad) {
    var submit = $(event.currentTarget);

    var url = form.attr('action');
    var formData = form.serialize();
    formData+='&'+form.attr('id')+'=1';
    formData+='&'+submit.attr('name')+'='+submit.attr('value');

    console.log('url: '+url);
    console.log('formData: '+formData);

    $.post(url, formData, function(response) {
        console.log(response);
        processResult(response, form, noLoad);
    });

    return false;
}

/**
 * Creates anchor that loads item when clicked
 * @param uuid
 */
function createClickableUUID(uuid) {
    var a = $('<a/>', {href:'#', text: uuid});
    var url = '/ii-weapp/crud-page.htm';
    var data = 'form_name=loadByUUIDForm&uuid=' + uuid + '&loadByUUIDForm=1&load=Load';
    a.click(function(){
        $.post(url, data, function(response) {
            processResult(response);
        })
    });
    return a;
}

$(document).ready(function() {
    eventHandlers();
    $('body > .topbar').scrollSpy();

    $('#create-load-ii-button').click(function(event) {
        var url = '';
        var extraData = 'pageAction=onCreateIiClick';
        $.getJSON(url, extraData, function(response) {
            processResult(response);
        });
        return false;
    });
    $('#create-ii-button').click(function(event) {
        var url = '';
        var extraData = 'pageAction=onCreateIiClick';
        $.getJSON(url, extraData, function(response) {
            processResult(response, null, true);
        });
        return false;
    });


    $('#loadByUUIDForm_load').live('click', function(event) {
        return processForm($('#loadByUUIDForm'), event);
    });

    $('#loadByMetaForm_load').live('click', function(event) {
        return processForm($('#loadByMetaForm'), event);
    });

    $('#updateMetaForm_updateAndLoad').live('click', function(event) {
        return processForm($('#updateMetaForm'), event);
    });

    $('#updateMetaForm_update').live('click', function(event) {
        return processForm($('#updateMetaForm'), event, true);
    });

    $('#updateComponentForm_updateAndLoad').live('click', function(event) {
        return processForm($('#updateComponentForm'), event);
    });

    $('#updateComponentForm_update').live('click', function(event) {
        return processForm($('#updateComponentForm'), event, true);
    });

    $('#deleteForm_delete').live('click', function(event) {
        return processForm($('#deleteForm'), event);
    });

});