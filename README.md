# 🍲 KainTayo

**Smart Filipino Family Meal Planner**
*An offline Android TV app, and proudly a vibe-coded AI slop project. 🤖*

> [!WARNING]
> **This is a vibe-coded, AI-generated project.** 🤖✨
> Nearly all of the code was written with AI tools. I gave the directions, ran it, poked at it, and said "no, not like that" a lot. Expect rough edges, questionable decisions, and the occasional bug that only appears on a real TV. You could call it AI slop, but hopefully the useful kind. 😄

KainTayo is an offline Android TV meal-planning app designed to help Filipino households plan affordable home-cooked meals and organize grocery shopping around their payday budget.

## 🤖 About the "Vibe Coding"

Let's be upfront about it:

- **AI wrote the code.** I described what I wanted, the AI generated it, and we went back and forth until it worked.
- **Humans steered.** Direction, design decisions, testing, and review came from a person. That doesn't make it perfect.
- **It's a portfolio project, not enterprise software.** Read the code with that in mind, and please don't deploy it in a hospital.
- **Bug reports and fixes are welcome.** Fixing AI slop is a perfectly good hobby.

## Features

- **Payday-based budgeting** — Set a budget for each payday cycle.
- **Household size** — Specify how many people will be eating.
- **Automatic meal planning** — Generate breakfast, lunch, and dinner suggestions for every day of the current budget cycle.
- **Filipino home-cooked meals** — Meal suggestions feature familiar and affordable ingredients commonly found in Philippine markets.
- **Consolidated grocery list** — Combine ingredients across meals to help organize shopping and reduce waste.
- **Budget estimates** — See estimated grocery costs and whether the plan fits the available budget.
- **Simple 3-step flow in Taglish** — Set the budget, check the grocery list, then check the meal plan, with large text and clear remote instructions.
- **Offline operation** — Meal planning and saved data are handled locally on the device. No account or internet connection is required.

## Payday Cycle

KainTayo organizes meal plans around two payday cycles:

- **15th to 29th** — or until the day before the next payday.
- **30th to 14th** — with the last calendar day treated as the 30th payday in months without a 30th day.

The meal plan and grocery list cover the entire current cycle.

## Built With

- Kotlin
- Jetpack Compose
- Android TV / Leanback
- Room Database
- AndroidX
- A large amount of AI-generated code 🤖

## Getting Started

1. Clone or download this repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync.
4. Connect or configure an Android TV emulator or compatible Android TV device.
5. Build and run the `app` configuration.

### Run Tests

From the project root:

```bash
./gradlew testDebugUnitTest
```

### Build Debug APK

```bash
./gradlew assembleDebug
```

The debug APK is generated under:

```text
app/build/outputs/apk/debug/
```

## Privacy and Data

KainTayo is designed to work offline.

- No account registration.
- No cloud backend.
- No remote meal-planning API.
- App data is stored locally on the device.

## Project Status

KainTayo is an open-source portfolio project. It is **vibe-coded and AI-assisted**, so treat it as a fun experiment rather than a polished product. Meal suggestions and cost estimates are rough guides, not nutrition advice or exact prices.

## Development Note

KainTayo is a **vibe-coded AI slop project**, built from start to finish with AI tools and human direction, testing, and review. 🍚

Is it slop? Yes. Does it plan your family's ulam for the whole payday cycle? Also yes. 😄

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.