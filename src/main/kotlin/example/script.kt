package example

import chrome.tabs.CreateProperties
import chrome.tabs.InjectDetails
import chrome.tabs.Tab
import kotlinjs.common.jsonAs
import kotlin.browser.window
import kotlin.js.Date

data class JianshuPost(val action: String, val title: String, val content: String)

fun main(args: Array<String>) {
    console.log("### chrome-extension-save-to-jianshu is loaded")

    val param = jsonAs<chrome.contextMenus.CreateProperties>().apply {
        title = "* => Jianshu"
        contexts = arrayOf("selection")
        onclick = { _, tab ->
            chrome.tabs.sendMessage(tab.id!!, "FETCH_SELECTION_HTML") { response ->
                val selection = response.toString()
                if (selection.isNotBlank()) {
                    saveToJianshu(selection, tab)
                } else {
                    window.alert("Please select some content")
                }
            }
        }
    }
    chrome.contextMenus.create(param)
}

private fun saveToJianshu(selection: String, triggerTab: Tab) {
    chrome.tabs.create(jsonAs<CreateProperties>().apply {
        this.url = "https://jianshu.com"
        this.active = false
    }) { jianshuTab: Tab ->
        console.log("Jianshu tab id: ${jianshuTab.id}")
        chrome.tabs.executeScript(jianshuTab.id!!, jsonAs<InjectDetails>().apply {
            file = "run-in-jianshu-tab.js"
        }) { _ ->
            chrome.runtime.lastError?.run {
                window.alert(JSON.stringify(this))
            } ?: run {
                console.log("### send NEW_JIANSHU_POST")
                console.log("---- title ----")
                console.log(triggerTab.title ?: today())
                console.log("---- content -----")
                console.log(selection)

                chrome.tabs.sendMessage(jianshuTab.id!!, JianshuPost(
                        action = "NEW_JIANSHU_POST",
                        title = triggerTab.title ?: today(),
                        content = selection
                )) { response ->
                    console.log("---------- response -----------")
                    console.log(response)
                }
            }
        }
    }
}

private fun today(): String {
    fun option() = jsonAs<Date.LocaleOptions>()
    val year = Date().toLocaleDateString("en-GB", option().apply { year = "numeric" })
    val month = Date().toLocaleDateString("en-GB", option().apply { month = "2-digit" })
    val day = Date().toLocaleDateString("en-GB", option().apply { day = "2-digit" })
    return "$year-$month-$day"
}
