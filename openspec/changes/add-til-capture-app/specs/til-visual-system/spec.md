## Purpose

The look-and-feel contract every surface honors: a permanently dark navy palette with a single vivid accent, gradient-lit surfaces, and motion that carries the user between states instead of snapping them there.

## ADDED Requirements

### Requirement: Permanently dark navy palette

The app SHALL render in a dark navy palette at all times, independent of the system light or dark setting, built on a near-black navy ground with a single vivid accent reserved for emphasis and interactive elements. The widget SHALL use the same palette as the app.

#### Scenario: Dark regardless of system setting

- **WHEN** the device is set to light mode
- **THEN** the app still renders in its dark navy palette
- **AND** no light-themed variant of any screen is presented

#### Scenario: Navy ground on every screen

- **WHEN** any screen is displayed
- **THEN** its background is a dark navy rather than a neutral grey or pure black

#### Scenario: Accent is reserved

- **WHEN** a screen contains both interactive and static elements
- **THEN** the vivid accent is applied to interactive elements, emphasized elements, and the accent-filled cards described below
- **AND** all other body content is rendered in the palette's foreground tones instead

#### Scenario: The widget matches the app

- **WHEN** the widget is displayed on the home screen
- **THEN** its colors are drawn from the same palette as the app's screens

### Requirement: Gradient-lit surfaces

Key surfaces — entry cards, screen headers, and the widget — SHALL be filled with gradients derived from the defined palette rather than flat single colors, and SHALL NOT introduce colors from outside that palette.

#### Scenario: Cards carry gradients

- **WHEN** an entry card is rendered
- **THEN** its surface is filled with a gradient rather than a flat color

#### Scenario: Gradients stay within the palette

- **WHEN** any gradient is rendered
- **THEN** every color stop within it comes from the defined palette

#### Scenario: Cards remain distinguishable from the ground

- **WHEN** a gradient-filled card sits on the screen background
- **THEN** the card's edges are discernible from the background behind it

#### Scenario: The feed fades at its scrolling edge

- **WHEN** feed content extends past the bottom edge of the scrolling area
- **THEN** a gradient scrim fades that content into the ground rather than cutting it off at a hard line

#### Scenario: The scrim never hides reachable content

- **WHEN** a user scrolls to the end of the feed
- **THEN** the last card is fully legible, unobscured by the scrim

### Requirement: Accent-filled cards punctuate the feed

A minority of entry cards SHALL be rendered filled with the vivid accent instead of the dark card gradient, so the feed reads with visual rhythm rather than as a uniform field. Roughly one card in five SHALL be accent-filled. The selection SHALL be derived from the entry's own identity so that it is stable: the same entry SHALL be accent-filled or not consistently, across scrolling, filtering, and app restarts.

#### Scenario: Some cards are accent-filled

- **WHEN** the feed displays enough entries to fill more than one screen
- **THEN** a minority of the cards are filled with the vivid accent while the rest carry the dark card gradient

#### Scenario: Selection is stable across scrolling

- **WHEN** a user scrolls a card off screen and back into view
- **THEN** that card is accent-filled if and only if it was accent-filled before

#### Scenario: Selection is stable across filtering

- **WHEN** a filter changes which entries are shown and a given entry remains visible
- **THEN** that entry's card keeps the same treatment it had before the filter changed

#### Scenario: Selection is stable across restarts

- **WHEN** the app is closed and reopened
- **THEN** each entry's card carries the same treatment it had in the previous session

#### Scenario: Accent-filled cards stay legible

- **WHEN** text is rendered on an accent-filled card
- **THEN** its contrast against the accent fill is at least 4.5:1

### Requirement: Ambient gradient arcs on sparse screens

Large sweeping gradient arcs SHALL be used as an ambient backdrop on screens that are otherwise sparse — the statistics screen and empty states — and SHALL NOT be drawn behind the timeline card feed, where they would compete with card content.

#### Scenario: Arcs behind the statistics screen

- **WHEN** the statistics screen is displayed
- **THEN** sweeping gradient arcs are drawn behind its content

#### Scenario: Arcs behind an empty state

- **WHEN** an empty state is displayed
- **THEN** sweeping gradient arcs are drawn behind its message

#### Scenario: No arcs behind the feed

- **WHEN** the timeline feed contains entries
- **THEN** no ambient arcs are drawn behind the cards

#### Scenario: Arcs never cost legibility

- **WHEN** text is placed over an area covered by an arc
- **THEN** its contrast against the arc at that point is at least 4.5:1

### Requirement: Animated transitions and motion

Changes of screen and changes of content SHALL be animated rather than swapped instantly: navigation between screens, cards entering the feed, and list changes caused by filtering SHALL all be carried by motion.

#### Scenario: Navigating between screens

- **WHEN** a user moves from one screen to another
- **THEN** the change is carried by an animated transition rather than an instant swap

#### Scenario: Cards entering the feed

- **WHEN** the timeline feed first appears
- **THEN** its cards animate into place rather than appearing fully formed at once

#### Scenario: Filtering animates the list

- **WHEN** a filter change adds or removes entries from the feed
- **THEN** the affected cards animate in or out and the remaining cards animate to their new positions

#### Scenario: Opening capture

- **WHEN** the capture surface is opened
- **THEN** it animates into view rather than appearing abruptly

### Requirement: Legibility over decoration

Decoration SHALL never cost readability. Text SHALL maintain a contrast ratio of at least 4.5:1 against its background, measured at the least favorable point of any gradient behind it, and layouts SHALL accommodate the system font scale without clipping or overlapping content.

#### Scenario: Text over a gradient

- **WHEN** text is placed over a gradient-filled surface
- **THEN** its contrast ratio against the background is at least 4.5:1 at the lowest-contrast point of that gradient

#### Scenario: Enlarged system font

- **WHEN** the system font scale is increased to its largest supported setting
- **THEN** text grows with it, cards grow to accommodate the taller text, and no text is clipped or overlapped

#### Scenario: Entry text takes precedence

- **WHEN** a card shows entry text alongside its date, time, and labels
- **THEN** the entry text is the most visually prominent element on the card

### Requirement: Respect for reduced motion

When the user has disabled or reduced system animations, the system SHALL degrade its animations to instant changes or simple cross-fades without any loss of function or content.

#### Scenario: Animations disabled at the system level

- **WHEN** the system animation scale is set to off
- **THEN** transitions resolve immediately or as a simple cross-fade
- **AND** every screen, control, and entry remains reachable and complete

#### Scenario: No function depends on animation

- **WHEN** animations are reduced or disabled
- **THEN** no information conveyed only by animation is lost, and no action becomes unavailable
