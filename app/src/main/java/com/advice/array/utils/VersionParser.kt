package com.advice.array.utils

object VersionParser {

    fun isGreaterOrEqual(version: String?, to: String, ignoreRevision: Boolean): Boolean {
        if (version == null)
            return false

        // Same string, same version
        if (version == to)
            return true

        val lhs = toVersion(version, ignoreRevision)
        val rhs = toVersion(to, ignoreRevision)

        return lhs >= rhs
    }

    private fun toVersion(version: String, ignoreRevision: Boolean): Version {
        val revisions = version.split("-")
        val revision = if (!ignoreRevision && revisions.size > 1) {
            revisions.lastOrNull()
        } else null

        val values = revisions.first().split(".").map { it.toInt() }

        return Version(values[0], values[1], values[2], revision)
    }

    data class Version(val major: Int, val minor: Int, val patch: Int, val revision: String?) :
        Comparable<Version> {

        override fun compareTo(other: Version): Int {
            if (major > other.major)
                return 1

            if (major < other.major)
                return -1

            if (minor > other.minor)
                return 1

            if (minor < other.minor)
                return -1

            if (patch > other.patch)
                return 1

            if (patch < other.patch)
                return -1

            // we are on a revision patch
            if (revision != null && other.revision == null)
                return -1

            // we are not on a revision patch
            if (revision == null && other.revision != null)
                return 1

            if (revision != null && other.revision != null) {
                val lhs = revision.replace("rc", "").toInt()
                val rhs = other.revision.replace("rc", "").toInt()
                return lhs.compareTo(rhs)
            }

            return 0
        }
    }
}