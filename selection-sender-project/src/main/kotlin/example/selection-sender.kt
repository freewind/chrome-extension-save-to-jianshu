package example

@JsName("getSelectionHtml")
external fun getSelectionHtml(): String

fun main(args: Array<String>) {
    chrome.runtime.onMessage.addListener { request, _, sendResponse ->
        console.log("----- on message ----- ")
        console.log(request)

        if (request == "FETCH_SELECTION_HTML") {
            val selection = getSelectionHtml()
            console.log("---- selection ----")
            console.log(selection)

            console.log("### send selection back")
            sendResponse(selection)
        }
    }
}
