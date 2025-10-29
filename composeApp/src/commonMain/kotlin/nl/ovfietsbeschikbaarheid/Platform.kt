package nl.ovfietsbeschikbaarheid

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform