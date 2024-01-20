package com.advice.array.models

import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*

sealed class Directory(val name: String, val lastModified: Date, val location: String) {
    class Folder(name: String, lastModified: Date, location: String) :
        Directory(name, lastModified, location)

    class File(name: String, val size: String, lastModified: Date, location: String) :
        Directory(name, lastModified, location)
}