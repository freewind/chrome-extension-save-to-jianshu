package example

import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLScriptElement
import kotlin.browser.document

data class JianshuPost(val action: String, val title: String, val content: String)

private const val titleId = "chrome-extension-save-to-jianshu->title"
private const val contentId = "chrome-extension-save-to-jianshu->content"

fun main(args: Array<String>) {
    console.log("### run-in-jianshu-tab is loaded")
    chrome.runtime.onMessage.addListener { request, _, sendResponse ->
        console.log("### chrome.runtime.onMessage")
        val message = request.unsafeCast<JianshuPost>()
        if (message.action == "NEW_JIANSHU_POST") {
            createDiv(titleId, message.title)
            createDiv(contentId, message.content)
            sendResponse("NEW_JIANSHU_POST ok")
        }
    }
    loadScript("functions-in-jianshu-page.js")
}

fun loadScript(file: String) {
    val script = document.createElement("script") as HTMLScriptElement
    script.src = chrome.extension.getURL(file)
    document.body!!.appendChild(script)
    console.log("Created script: $script")
}

private fun createDiv(id: String, content: String) {
    val div = document.createElement("div") as HTMLDivElement
    div.id = id
    div.innerHTML = content
    document.body!!.appendChild(div)
    console.log("Created div: $div")
}

