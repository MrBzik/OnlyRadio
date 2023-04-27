package com.example.radioplayer.utils

import com.example.radioplayer.adapters.models.TagWithGenre

const val TAG_BY_PERIOD = "- BY PERIOD -"
const val TAG_BY_GENRE = "-POPULAR GENRES-"
const val TAG_BY_SUB_GENRE = "-CROSS & SUBGENRES-"
const val TAG_BY_MINDFUL = "-MINDFULNESS-"
const val TAG_BY_ORIGIN = "-BY ORIGIN-"
const val TAG_BY_TALK = "-TALK, NEWS, etc-"
const val TAG_BY_RELIGION = "-RELIGION-"
const val TAG_BY_OTHER = "-OTHER-"
const val TAG_BY_CLASSIC = "-ART, CLASSICAL-"
const val TAG_BY_EXPERIMENTAL = "-EXPERIMENTAL-"
const val TAG_BY_SPECIAL = "-CHARTS, DJ, etc.-"

val listOfTagsSimple = listOf("charts", "hits", "top")

val tagsListSpecial = setOf(
    TagWithGenre.Tag("adult top 40", 23, 23),
    TagWithGenre.Tag("charts", 241, 118),
    TagWithGenre.Tag("classic hits", 462, 458),
    TagWithGenre.Tag("contemporary hits", 215, 145),
    TagWithGenre.Tag("dance top40", 12, 12),
    TagWithGenre.Tag("dj mixes", 24, 24),
    TagWithGenre.Tag("dj sets", 36, 36),
    TagWithGenre.Tag("freeform", 62, 57),
    TagWithGenre.Tag("hits", 2103, 611),
    TagWithGenre.Tag("no ads", 65, 65),
    TagWithGenre.Tag("top 100", 117, 114),
    TagWithGenre.Tag("top 40", 836, 687),
    TagWithGenre.Tag("top charts", 115, 115),
    TagWithGenre.Tag("top hits", 189, 189)
)


val byPeriodTagsSimple = setOf("30s", "40s",
"50s", "60s", "70s", "80s", "90s", "00s", "10s", "decades","golden", "goldies","medieval","nostalgia",
"oldies", "oldschool", "old time radio", "retro")

val tagsListByPeriod = setOf(
    TagWithGenre.Tag("20s", 34, 18),
    TagWithGenre.Tag("30s", 23, 12),
    TagWithGenre.Tag("40s", 30, 12),
    TagWithGenre.Tag("50s", 76, 58),
    TagWithGenre.Tag("60s", 222, 183),
    TagWithGenre.Tag("70s", 458, 365),
    TagWithGenre.Tag("80s", 996, 790),
    TagWithGenre.Tag("90s", 755, 599),
    TagWithGenre.Tag("00s", 223, 117),
    TagWithGenre.Tag("10s", 72, 22),
    TagWithGenre.Tag("decades", 77, 77),
    TagWithGenre.Tag("golden music", 25, 25),
    TagWithGenre.Tag("goldies", 44, 44),
    TagWithGenre.Tag("nostalgia", 33, 33),
    TagWithGenre.Tag("oldies", 985, 797),
    TagWithGenre.Tag("oldschool", 23, 19),
    TagWithGenre.Tag("old time radio", 42, 42),
    TagWithGenre.Tag("retro", 208, 168)
)

val byGenreTagsSimple = setOf(
    "acoustic","adult","alternative","ambient","anime", "avant-garde",
    "bachata", "balada", "bass", "beat", "blues", "boogie", "classic","club","conservative",
    "contemporary","country", "covers",
    "dance","disco","dj", "drama", "drum and bass","dubstep",
    "eclectic","edm","electro","experimental","flamenco",
    "folk","freeform","funk","garage", "gothic","groove","guitar","hardcore", "hardstyle",
    "heavy metal","hip-hop","hiphop","house","independent","indie","industrial",
    "instrumental","jazz","jungle", "lifestyle","lo-fi","lounge",
    "mainstream","metal", "mix",
    "new age","new wave", "noise", "opera","orchestral","organ", "party","patriot", "piano","pop",
    "progressive", "psy", "punk","r&b","rap","reggae","remixes","rnb","rock","romance",
    "romantic","romántica","salsa",
    "schlager", "surf music", "synth", "symphony", "swing","techno",


)


val tagsListByGenre = setOf(

    TagWithGenre.Tag("acoustic", 33, 25),
    TagWithGenre.Tag("adult contemporary", 725, 513),
    TagWithGenre.Tag("alternative", 856, 466),
    TagWithGenre.Tag("bass", 173, 27),
    TagWithGenre.Tag("beat", 166, 14),
    TagWithGenre.Tag("blues", 323, 205),
    TagWithGenre.Tag("boogie", 12, 10),
    TagWithGenre.Tag("club", 172, 82),
    TagWithGenre.Tag("conservative", 53, 19),
    TagWithGenre.Tag("country", 674, 512),
    TagWithGenre.Tag("covers", 17, 17),
    TagWithGenre.Tag("dance", 1393, 857),
    TagWithGenre.Tag("disco", 431, 207),
    TagWithGenre.Tag("drum and bass", 65, 65),
    TagWithGenre.Tag("dubstep", 48, 45),
    TagWithGenre.Tag("eclectic", 174, 154),
    TagWithGenre.Tag("edm", 196, 176),
    TagWithGenre.Tag("electro", 1322, 206),
    TagWithGenre.Tag("eurodance", 87, 87),
    TagWithGenre.Tag("folk", 849, 529),
    TagWithGenre.Tag("funk", 319, 206),
    TagWithGenre.Tag("garage", 66, 22),
    TagWithGenre.Tag("gothic", 89, 66),
    TagWithGenre.Tag("groove", 60, 34),
    TagWithGenre.Tag("guitar", 46, 23),
    TagWithGenre.Tag("hiphop", 313, 265),
    TagWithGenre.Tag("house", 952, 476),
    TagWithGenre.Tag("independent", 53, 41),
    TagWithGenre.Tag("indie", 485, 296),
    TagWithGenre.Tag("instrumental", 155, 139),
    TagWithGenre.Tag("jazz", 1191, 809),
    TagWithGenre.Tag("jungle", 29, 27),
    TagWithGenre.Tag("love", 216, 61),
    TagWithGenre.Tag("mainstream", 278, 241),
    TagWithGenre.Tag("metal", 653, 269),
    TagWithGenre.Tag("new age", 51, 47),
    TagWithGenre.Tag("new wave", 74, 74),
    TagWithGenre.Tag("party", 143, 89),
    TagWithGenre.Tag("pop", 7569, 3638),
    TagWithGenre.Tag("punk", 240, 108),
    TagWithGenre.Tag("r&b", 147, 100),
    TagWithGenre.Tag("rap", 444, 236),
    TagWithGenre.Tag("reggae", 301, 171),
    TagWithGenre.Tag("remixes", 25, 25),
    TagWithGenre.Tag("rnb", 216, 156),
    TagWithGenre.Tag("rock", 4831, 2077),
    TagWithGenre.Tag("rock'n'roll", 28, 28),
    TagWithGenre.Tag("romance", 45, 45),
    TagWithGenre.Tag("schlager", 199, 165),
    TagWithGenre.Tag("synth", 104, 11),
    TagWithGenre.Tag("swing", 56, 44),
    TagWithGenre.Tag("techno", 337, 246),
    TagWithGenre.Tag("urban", 449, 117),
    TagWithGenre.Tag("vocal", 114, 21)

)

val tagsListBySubGenre = setOf(
    TagWithGenre.Tag("acid jazz", 8, 8),
    TagWithGenre.Tag("adult album alternative", 60, 60),
    TagWithGenre.Tag("alternative rock", 245, 232),
    TagWithGenre.Tag("art rock", 26, 23),
    TagWithGenre.Tag("black metal", 28, 25),
    TagWithGenre.Tag("blues rock", 42, 42),
    TagWithGenre.Tag("classic dance", 10, 10),
    TagWithGenre.Tag("classic jazz", 36, 36),
    TagWithGenre.Tag("classic rock", 640, 601),
    TagWithGenre.Tag("club dance", 26, 26),
    TagWithGenre.Tag("chiptune", 31, 25),
    TagWithGenre.Tag("country rock", 11, 11),
    TagWithGenre.Tag("death metal", 34, 28),
    TagWithGenre.Tag("electronic dance music", 44, 44),
    TagWithGenre.Tag("electro house", 41, 41),
    TagWithGenre.Tag("folk rock", 25, 25),
    TagWithGenre.Tag("free jazz", 15, 15),
    TagWithGenre.Tag("glam rock", 23, 20),
    TagWithGenre.Tag("gothic rock", 12, 12),
    TagWithGenre.Tag("hard rock", 166, 162),
    TagWithGenre.Tag("heavy metal", 152, 148),
    TagWithGenre.Tag("heavy rock", 29, 24),
    TagWithGenre.Tag("hot adult contemporary", 127, 127),
    TagWithGenre.Tag("indie rock", 90, 90),
    TagWithGenre.Tag("jazz fusion", 14, 14),
    TagWithGenre.Tag("modern rock", 22, 22),
    TagWithGenre.Tag("nu-jazz", 21, 21),
    TagWithGenre.Tag("piano jazz", 12, 12),
    TagWithGenre.Tag("progressive rock", 89, 85),
    TagWithGenre.Tag("punk rock", 43, 38),
    TagWithGenre.Tag("rhythm and blues", 39, 32),
    TagWithGenre.Tag("slow rock", 22, 17),
    TagWithGenre.Tag("smooth jazz", 146, 146),
    TagWithGenre.Tag("soft adult contemporary", 32, 32),
    TagWithGenre.Tag("soft rock", 113, 96),
    TagWithGenre.Tag("symphonic metal", 10, 10),
    TagWithGenre.Tag("symphonic rock", 25, 19),
    TagWithGenre.Tag("synthwave", 17, 16),
    TagWithGenre.Tag("vocal jazz", 23, 23)
)

val tagsListExperimental = setOf(
    TagWithGenre.Tag("avant-garde", 26, 22),
    TagWithGenre.Tag("experimental", 108, 95),
    TagWithGenre.Tag("hardstyle", 46, 46),
    TagWithGenre.Tag("hardcore", 93, 64),
    TagWithGenre.Tag("industrial", 68, 54),
    TagWithGenre.Tag("lo-fi", 11, 11),
    TagWithGenre.Tag("noise", 24, 18),
    TagWithGenre.Tag("progressive", 209, 52),
    TagWithGenre.Tag("psychedelic", 42, 21),
    TagWithGenre.Tag("psychill", 12, 10),
    TagWithGenre.Tag("psytrance", 34, 24),
    TagWithGenre.Tag("stoner", 17, 5),
    TagWithGenre.Tag("trance", 275, 179),
    TagWithGenre.Tag("trip hop", 12, 12),
    TagWithGenre.Tag("underground", 43, 39),
)










val tagsListClassics = setOf(
    TagWithGenre.Tag("contemporary classical", 6, 6),
    TagWithGenre.Tag("drama", 27, 18),
    TagWithGenre.Tag("medieval", 19, 19),
    TagWithGenre.Tag("modern classical", 9, 9),
    TagWithGenre.Tag("classical", 1707, 1550),
    TagWithGenre.Tag("classical baroque", 10, 10),
    TagWithGenre.Tag("classical piano", 19, 19),
    TagWithGenre.Tag("opera", 65, 55),
    TagWithGenre.Tag("orchestral", 20, 14),
    TagWithGenre.Tag("organ", 29, 10),
    TagWithGenre.Tag("piano", 94, 50),
    TagWithGenre.Tag("musica clasica romantica", 41, 41),
    TagWithGenre.Tag("symphony", 20, 16),
    TagWithGenre.Tag("violin", 7, 7)
)


val specialTagsSimple = setOf("amor","atmospheric", "calm", "chill","downtempo", "fantasy", "flac", "love", "meditation", "multicultural",
"music for study", "nature", "no ads", "paranormal", "relax",
"sleep", "slow", "smooth", "soft",  "soul", "spiritual","workout")

val tagsListMindful = setOf(
    TagWithGenre.Tag("ambient", 374, 271),
    TagWithGenre.Tag("atmospheric", 12, 10),
    TagWithGenre.Tag("calm", 12, 12),
    TagWithGenre.Tag("chill", 525, 123),
    TagWithGenre.Tag("downtempo", 66, 65),
    TagWithGenre.Tag("easy listening", 193, 192),
    TagWithGenre.Tag("fantasy", 10, 10),
    TagWithGenre.Tag("flac", 11, 11),
    TagWithGenre.Tag("lounge", 376, 246),
    TagWithGenre.Tag("meditation", 42, 42),
    TagWithGenre.Tag("nature", 36, 20),
    TagWithGenre.Tag("relax", 370, 167),
    TagWithGenre.Tag("sleep", 39, 33),
    TagWithGenre.Tag("slow", 58, 23),
    TagWithGenre.Tag("smooth", 238, 59),
    TagWithGenre.Tag("soft music", 27, 27),
    TagWithGenre.Tag("soul", 375, 281),
    TagWithGenre.Tag("spiritual", 38, 25)
)



val byOriginTagsSimple = setOf("aboriginal", "african", "afrobeats", "americana", "arabic", "asian",  "banda","bollywood",
    "caribbean", "celtic", "ethnic","greek", "grupera", "grupero", "indian music",
    "latin", "méxico", "national", "spanish")


val tagsListByOrigin = setOf(
    TagWithGenre.Tag("aboriginal", 11, 10),
    TagWithGenre.Tag("african", 50, 9),
    TagWithGenre.Tag("afrobeats", 23, 23),
    TagWithGenre.Tag("americana", 224, 43),
    TagWithGenre.Tag("amor", 102, 31),
    TagWithGenre.Tag("arabic", 57, 31),
    TagWithGenre.Tag("asian", 22, 10),
    TagWithGenre.Tag("bachata", 41, 26),
    TagWithGenre.Tag("balada", 663, 110),
    TagWithGenre.Tag("banda", 402, 186),
    TagWithGenre.Tag("bollywood", 75, 57),
    TagWithGenre.Tag("caribbean", 35, 13),
    TagWithGenre.Tag("celtic", 27, 27),
    TagWithGenre.Tag("dancehall", 36, 36),
    TagWithGenre.Tag("deutschrock", 18, 18),
    TagWithGenre.Tag("ethnic", 51, 34),
    TagWithGenre.Tag("flamenco", 9, 9),
    TagWithGenre.Tag("folklore", 27, 27),
    TagWithGenre.Tag("greek", 709, 389),
    TagWithGenre.Tag("greek folk", 108, 60),
    TagWithGenre.Tag("grupera", 316, 282),
    TagWithGenre.Tag("grupero", 209, 209),
    TagWithGenre.Tag("indian music", 9, 9),
    TagWithGenre.Tag("italo dance", 14, 9),
    TagWithGenre.Tag("kurdish folk music", 7, 7),
    TagWithGenre.Tag("latin", 1892, 118),
    TagWithGenre.Tag("latin jazz", 10, 10),
    TagWithGenre.Tag("méxico", 1396, 1090),
    TagWithGenre.Tag("moi merino", 633, 633),
    TagWithGenre.Tag("regional", 2502, 339),
    TagWithGenre.Tag("romanian folk", 12, 12),
    TagWithGenre.Tag("romántica", 280, 81),
    TagWithGenre.Tag("russian rock", 20, 20),
    TagWithGenre.Tag("salsa", 141, 84),
    TagWithGenre.Tag("spanish", 212, 50),
    TagWithGenre.Tag("traditional", 370, 63),
    TagWithGenre.Tag("tropical", 177, 84),
    TagWithGenre.Tag("viking", 4, 2),
)



val talkNewsTagsSimple = setOf("comedy","commercial", "community",
    "conspiracy theories","culture",
    "easy listening","economics","education","entertainment","entretenimiento",
    "fashion", "film",
    "football", "government", "humor",
    "information","international", "interviews","literature","live","local","movie", "news","noticias",
    "open air", "podcast",
    "politics","regional","science", "soccer", "social", "soundtrack","speech","sport",
    "storytelling","talk","travel", "tv")


val tagsListByTalk = setOf(
    TagWithGenre.Tag("comedy", 66, 53),
    TagWithGenre.Tag("commercial", 351, 235),
    TagWithGenre.Tag("community", 572, 98),
    TagWithGenre.Tag("conservative talk", 34, 34),
    TagWithGenre.Tag("conspiracy theories", 12, 12),
    TagWithGenre.Tag("culture", 250, 214),
    TagWithGenre.Tag("economics", 51, 51),
    TagWithGenre.Tag("education", 67, 32),
    TagWithGenre.Tag("entertainment", 216, 214),
    TagWithGenre.Tag("entretenimiento", 685, 684),
    TagWithGenre.Tag("fashion", 12, 11),
    TagWithGenre.Tag("film", 41, 22),
    TagWithGenre.Tag("football", 44, 31),
    TagWithGenre.Tag("government", 29, 14),
    TagWithGenre.Tag("humor", 22, 20),
    TagWithGenre.Tag("information", 333, 279),
    TagWithGenre.Tag("international", 211, 175),
    TagWithGenre.Tag("interviews", 8, 8),
    TagWithGenre.Tag("literature", 52, 52),
    TagWithGenre.Tag("live", 211, 54),
    TagWithGenre.Tag("local", 1308, 87),
    TagWithGenre.Tag("movie", 64, 28),
    TagWithGenre.Tag("news", 3756, 2542),
    TagWithGenre.Tag("noticias", 923, 255),
    TagWithGenre.Tag("open air", 28, 14),
    TagWithGenre.Tag("paranormal", 15, 15),
    TagWithGenre.Tag("podcast", 26, 19),
    TagWithGenre.Tag("politics", 82, 75),
    TagWithGenre.Tag("science", 30, 16),
    TagWithGenre.Tag("soccer", 13, 10),
    TagWithGenre.Tag("social", 88, 42),
    TagWithGenre.Tag("speech", 252, 23),
    TagWithGenre.Tag("sport", 717, 210),
    TagWithGenre.Tag("sports talk", 37, 37),
    TagWithGenre.Tag("storytelling", 21, 21),
    TagWithGenre.Tag("talk", 2313, 1248),
    TagWithGenre.Tag("talk & speech", 227, 227),
    TagWithGenre.Tag("traffic", 172, 132),
    TagWithGenre.Tag("travel", 13, 10),
    TagWithGenre.Tag("tv", 162, 102),
)


val religionTagsSimple = setOf("bible","catholic","christian","christmas","gospel","islamic", "religion","religious")

val tagsListReligion = setOf(
    TagWithGenre.Tag("christian contemporary", 87, 87),
    TagWithGenre.Tag("bible", 68, 63),
    TagWithGenre.Tag("catholic", 152, 135),
    TagWithGenre.Tag("christian", 1135, 807),
    TagWithGenre.Tag("christian praise&worship", 35, 35),
    TagWithGenre.Tag("christian rock", 18, 18),
    TagWithGenre.Tag("christian talk", 14, 10),
    TagWithGenre.Tag("christmas", 195, 67),
    TagWithGenre.Tag("gospel", 225, 183),
    TagWithGenre.Tag("islamic", 38, 35),
    TagWithGenre.Tag("religion", 193, 191),
)


val otherTagsSimple = setOf("children", "disney", "juvenil", "kids",
     "moi merino",  "singer-songwriter", "student", "university")

val tagsListOther = setOf(
    TagWithGenre.Tag("anime", 82, 52),
    TagWithGenre.Tag("children", 95, 82),
    TagWithGenre.Tag("disney", 21, 10),
    TagWithGenre.Tag("juvenil", 247, 247),
    TagWithGenre.Tag("kids", 58, 58),
    TagWithGenre.Tag("lifestyle", 80, 80),
    TagWithGenre.Tag("multicultural", 45, 44),
    TagWithGenre.Tag("music for study", 8, 8),
    TagWithGenre.Tag("national", 242, 21),
    TagWithGenre.Tag("patriot", 12, 8),
    TagWithGenre.Tag("singer-songwriter", 22, 22),
    TagWithGenre.Tag("soundtrack", 98, 40),
    TagWithGenre.Tag("student", 106, 30),
    TagWithGenre.Tag("surf music", 11, 11),
    TagWithGenre.Tag("university", 312, 38),
    TagWithGenre.Tag("workout", 21, 21)
)

val QUEEN = "Exclusive Radio – Queen"
val QUEEN_URL = "https://streaming.exclusive.radio/er/queen/icecast.audio"

val QUEEN_NET = "Queenradio.net"
val QUEEN_NET_URL = "http://n06.radiojar.com/4gw72qm8u68uv?rj-ttl=5&rj-tok=AAABhuNtzuUAEIBZr_gdGrrSlQ"

val QEEN_RMF = "RMF Queen"
val QEEN_RMF_URL = "http://195.150.20.8/QUEEN"

val QUEEN_VIRGIN = "Virgin Radio Rockstar: Queen"
val QUEEN_VIRGIN_URL = "https://icy.unitedradio.it/Virgin_05.mp3"

 // BEATLES

val BEATLES = "John Paul George and Ringo"
val BEATLES_URL = "John Paul George and Ringo"



