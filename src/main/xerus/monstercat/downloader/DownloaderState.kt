package xerus.monstercat.downloader

import xerus.ktutil.javafx.properties.AbstractObservableValue

class DownloaderState(total: Int, success: Int = 0, errors: Int = 0) : AbstractObservableValue<DownloaderState>(true) {
	var total = total
		private set
	var errors = errors
		private set
	var success = success
		private set
	var isDone = false
	
	override fun getValue() = this
	
	fun success() {
		success++
		listeners.notifyChange(this, this)
	}
	
	fun error(exception: Throwable) {
		errors++
		listeners.notifyChange(this, this)
	}
	
	fun cancel() {
		total--
		listeners.notifyChange(this, this)
	}
	
	fun done() {
		isDone = true
		listeners.notifyChange(this, this)
	}
	
	override fun toString() = "DownloaderState(total=$total, errors=$errors, success=$success)"
	
}