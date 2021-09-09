package it.simone.bookyoulove

object Constants {
    const val START_DATE_SETTER = 0
    const val END_DATE_SETTER = 1

    const val READING_BOOK_STATE = 0
    const val ENDED_BOOK_STATE = 1
    const val TBR_BOOK_STATE = 2

    const val PAPER_SUPPORT = "Paper"
    const val EBOOK_SUPPORT = "eBook"
    const val AUDIOBOOK_SUPPORT = "AudioBook"

    const val SORT_START_DATE = 0
    const val SORT_END_DATE = 1
    const val SORT_BY_TITLE = 0
    const val SORT_BY_AUTHOR = 1

    const val SEARCH_BY_TITLE = 0
    const val SEARCH_BY_AUTHOR = 1
    const val SEARCH_BY_RATE = 2
    const val SEARCH_BY_YEAR = 3
    const val SEARCH_BY_TITLE_OR_AUTHOR = 4

    const val TAG = "BookYouLove"

    const val TOTAL_RATE = "total"
    const val STYLE_RATE = "style"
    const val PLOT_RATE = "plot"
    const val EMOTIONS_RATE = "emotions"
    const val CHARACTER_RATE = "character"

    const val ISBN_NO_ERROR = 0
    const val ISBN_FIND_ITEM_ERROR = 1
    const val ISBN_INTERNET_ACCESS_ERROR = 2

    const val GOOGLE_DRIVE_BACKUP_NAME = "BookYouLoveDatabaseBackup"

    const val ENDED_DETAIL_ENTRY_CODE_FROM_CHARTS = 1

    const val QUOTE_OF_THE_DAY_FAVORITE_SWITCH_INTENT = "it.simone.bookyoulove.QUOTE_OF_THE_DAY_FAVOURITE_SWITCH_INTENT"
    const val QUOTE_FROM_WIDGET_INTENT = "it.simone.bookyoulove.TAKE_QUOTE_FROM_WIDGET_INTENT"
}