package com.owlunit.web.lib

import net.liftweb.http.S
import xml.Text

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object SnippetUtils {

  def noticeBox(text: String, id: String = "") = S.notice(id,
    <div class="alert alert-info">
      <a class="close" data-dismiss="alert">×</a>
      <strong>{"HEY! "}</strong>
      {text}
    </div>
  )

  def warningBox(text: String, id: String = "") = S.notice(id,
    <div class="alert">
      <a class="close" data-dismiss="alert">×</a>
      <strong>{"HEY! "}</strong>
      {text}
    </div>
  )

  def errorBox(text: String, id: String = "") = S.notice(id,
    <div class="alert alert-error">
      <a class="close" data-dismiss="alert">×</a>
      <strong>{"HEY! "}</strong>
      {text}
    </div>
  )
}