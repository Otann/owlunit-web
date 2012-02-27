package com.owlunit.web.crud.control;

import com.google.gson.Gson;
import com.owlunit.core.ii.Ii;
import org.apache.click.ActionResult;
import org.json.simple.JSONObject;

import java.util.Collection;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class Utils {
    
    public enum Result {
        ERROR("error"),
        WARN("warn"),
        SUCCESS("success"),
        OK("ok"),
        INFO("info");
        
        private String message;

        private Result(String message) {
            this.message = message;
        }
    }

    private static Gson gson = new Gson();

    @SuppressWarnings("unchecked")
    public static ActionResult createObjectResult(String html, Object object, String text, Result type) {
        JSONObject result = new JSONObject();
        result.put("html", html);
        result.put("data", object != null ? gson.toJson(object) : null);
        result.put("text", text);
        result.put("type", type != null ? type.message : null);
        return new ActionResult(result.toString(), ActionResult.JSON);
    }

    public static ActionResult createMessageResult(String text, Result type) {
        return createObjectResult(null, null, text, type);
    }

    public static ActionResult createItemResult(String html, Ii item) {
        return createObjectResult(html, item, null, null);
    }

    public static ActionResult createItemsResult(String html, Collection<Ii> items) {
        return createObjectResult(html, items, null, null);
    }
}
