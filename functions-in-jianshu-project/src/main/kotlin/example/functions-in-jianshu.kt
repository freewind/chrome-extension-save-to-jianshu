package example

import kotlinjs.common.jsonAs
import org.w3c.fetch.INCLUDE
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.json

private const val titleId = "chrome-extension-save-to-jianshu->title"
private const val contentId = "chrome-extension-save-to-jianshu->content"

private const val NotebookId = "27748644"

fun main(args: Array<String>) {
    console.log("### functions-in-jianshu is loaded")

    console.log("Check new-post placeholders")
    val title = document.getElementById(titleId)?.textContent?.trim() ?: ""
    val content = document.getElementById(contentId)?.innerHTML?.trim() ?: ""
    if (title.isNotEmpty() && content.isNotEmpty()) {
        console.log("title: $title")
        console.log("content: $content")
        clearDiv(titleId, contentId)
        createNewPost(title, content)
    } else {
        console.log("no post found, exit")
    }

}

private fun clearDiv(vararg ids: String) {
    ids.forEach { document.getElementById(it)?.innerHTML = "" }
}

interface NewPostBody {
    var notebook_id: String
    var title: String
    var at_bottom: Boolean
}

private val Headers = json(
        "Accept" to "application/json",
        "Accept-Encoding" to "gzip, deflate, br",
        "Accept-Language" to "en-US,en;q=0.9,zh-TW;q=0.8,zh;q=0.7",
        "Cache-Control" to "no-cache",
        "Connection" to "keep-alive",
        "Content-Type" to "application/json; charset=UTF-8",
        "Host" to "www.jianshu.com",
        "Pragma" to "no-cache"
)

private fun createNewPost(title: String, content: String) {
    console.log("------------------- createNewPost ------------------")

    window.fetch("https://www.jianshu.com/author/notes", jsonAs<RequestInit>().apply {
        method = "POST"
        referrer = "https://www.jianshu.com/writer"
        credentials = RequestCredentials.INCLUDE
        headers = Headers
        body = JSON.stringify(jsonAs<NewPostBody>().apply {
            this.notebook_id = NotebookId
            this.title = title
            this.at_bottom = true
        })
    }).then { response ->
        if (response.ok) {
            response.text().then { text ->
                console.log(text)
                val json = JSON.parse<dynamic>(text)
                val id = json["id"] as Number
                updateContent(id.toString(), title, content)
            }
        } else {
            window.alert(response.statusText)
        }
    }
}

interface UpdateContentBody {
    var id: String
    var autosave_control: Int
    var title: String
    var content: String
}

private fun updateContent(noteId: String, title: String, content: String) {
    console.log("------------------- updateContent ------------------")
    window.fetch("https://www.jianshu.com/author/notes/$noteId", jsonAs<RequestInit>().apply {
        method = "PUT"
        credentials = RequestCredentials.INCLUDE
        headers = Headers
        body = JSON.stringify(jsonAs<UpdateContentBody>().apply {
            this.id = noteId
            this.autosave_control = 1
            this.title = title
            this.content = content
        })
    }).then { response ->
        if (response.ok) {
            publish(noteId)
        } else {
            window.alert(response.statusText)
        }
    }
}

private fun publish(noteId: String) {
    console.log("------------------- publish ------------------")
    window.fetch("https://www.jianshu.com/author/notes/$noteId/publicize", jsonAs<RequestInit>().apply {
        method = "POST"
        headers = Headers
        credentials = RequestCredentials.INCLUDE
    }).then { response ->
        if (response.ok) {
            window.alert("published successfully")
        } else {
            window.alert(response.statusText)
        }
    }

}