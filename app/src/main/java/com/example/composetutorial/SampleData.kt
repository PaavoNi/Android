package com.example.composetutorial

object SampleData {
    // Sample conversation data
    val conversationSample = listOf(
        Message("Paavo", "Test...Test...Test..."),
        Message(
            "Paavo",
            """
            List of Android versions:
            |Android KitKat (API 19)
            |Android Lollipop (API 21)
            |Android Marshmallow (API 23)
            |Android Nougat (API 24)
            |Android Oreo (API 26)
            |Android Pie (API 28)
            |Android 10 (API 29)
            |Android 11 (API 30)
            |Android 12 (API 31)
            """.trimIndent()
        ),

        Message("Paavo", "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
        Message("Paavo", "Nullam sem neque, dapibus non nisi ut, consequat pretium purus. Nam mattis, nisi a elementum convallis, erat turpis convallis augue, accumsan placerat purus diam eget nisi."),
        Message("Paavo", "Aliquam ut nunc pellentesque, blandit lorem sit amet, sodales tortor. Vivamus vitae ipsum tellus. Vestibulum posuere lorem nec cursus pulvinar. Nulla facilisi. Phasellus eu neque ut metus posuere finibus. Morbi nibh ante, tempus nec augue sit amet, interdum faucibus nunc. "),
        Message("Paavo", "Nam tristique elit et arcu porttitor, ac auctor neque aliquam."),
        Message("Paavo", "Aenean dapibus hendrerit bibendum. Nullam pharetra porta nisi, sed porttitor magna feugiat quis."),
        Message("Paavo", "Duis ut enim ipsum. Aliquam risus mauris, volutpat at erat sed, auctor venenatis ipsum."),
        Message("Paavo", "Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos."),
        Message("Paavo", "Fusce interdum, dolor a tincidunt ultricies, neque libero eleifend tortor, at dignissim justo nunc et nunc. "),
    )
}
