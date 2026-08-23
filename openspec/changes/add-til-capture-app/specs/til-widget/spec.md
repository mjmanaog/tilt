## Purpose

The home-screen surface that makes capture nearly free: one tap to a text field floating over the home screen, and a rotating glimpse of something already learned so old entries keep surfacing.

## ADDED Requirements

### Requirement: One-tap capture from the home screen

The widget SHALL provide a tap target that opens a capture surface presented over the home screen, with the text field already focused and the keyboard raised, and with the home screen still visible behind it. Reaching capture SHALL NOT require navigating the app first.

#### Scenario: Tapping the capture target

- **WHEN** a user taps the widget's capture target
- **THEN** a capture surface appears over the home screen with a focused text field and the keyboard raised
- **AND** the home screen remains visible behind that surface

#### Scenario: The capture surface offers text and labels

- **WHEN** the capture surface is open
- **THEN** it offers a field for the entry text, a way to attach labels including suggestions from previously used labels, and a save action

#### Scenario: Capture does not open the full app

- **WHEN** a user taps the widget's capture target
- **THEN** the app's timeline is not brought to the foreground

### Requirement: Saving and dismissing from the widget capture surface

Saving from the widget capture surface SHALL create an entry indistinguishable from one created inside the app, and SHALL return the user to the home screen. Dismissing without saving SHALL create nothing and discard what was typed.

#### Scenario: Saving an entry

- **WHEN** a user types text on the capture surface and activates save
- **THEN** an entry is persisted with that text, any attached labels, and the current local date and time
- **AND** the capture surface closes and the home screen is returned to
- **AND** the entry appears in the app's timeline the next time it is opened

#### Scenario: Blank text cannot be saved

- **WHEN** the capture surface holds no text or only whitespace
- **THEN** the save action is unavailable and no entry is created

#### Scenario: Dismissing by tapping outside

- **WHEN** a user taps outside the capture surface
- **THEN** the surface closes, no entry is created, and the typed text is discarded

#### Scenario: Dismissing with the back gesture

- **WHEN** a user performs the system back gesture while the capture surface is open
- **THEN** the surface closes, no entry is created, and the home screen is returned to

### Requirement: Rotating display of a past entry

When at least one entry exists, the widget SHALL display the text and capture date of one previously captured entry, selected at random from all entries, and the entry shown SHALL change over time as the widget refreshes.

#### Scenario: Showing a past entry

- **WHEN** the widget refreshes and at least one entry exists
- **THEN** it displays one entry's text together with the date that entry was captured

#### Scenario: The selection varies

- **WHEN** the widget refreshes repeatedly while many entries exist
- **THEN** the entry displayed is drawn at random from all existing entries rather than being fixed to one

#### Scenario: Only one entry exists

- **WHEN** exactly one entry exists
- **THEN** the widget displays that entry

#### Scenario: Opening the displayed entry

- **WHEN** a user taps the displayed past entry
- **THEN** the app opens showing that entry

### Requirement: Prompt when there is nothing to show

When no entries exist, the widget SHALL display a prompt inviting capture rather than an empty or broken surface, while keeping the capture target usable.

#### Scenario: Empty prompt

- **WHEN** the widget is displayed and no entries exist
- **THEN** it shows a prompt asking what the user learned today, in place of a past entry

#### Scenario: Capture works from the empty state

- **WHEN** a user taps the widget while it is showing the empty prompt
- **THEN** the capture surface opens as it does in any other state

#### Scenario: Leaving the empty state

- **WHEN** the first entry is captured
- **THEN** the widget replaces the prompt with a past entry at its next refresh

### Requirement: The widget reflects current data

The widget SHALL keep its displayed content consistent with the stored entries, updating after entries are created, edited, or deleted from either the app or the widget capture surface.

#### Scenario: Reflecting a save made in the app

- **WHEN** an entry is captured inside the app
- **THEN** the widget's pool of displayable entries includes it at the next refresh

#### Scenario: Reflecting a save made from the widget

- **WHEN** an entry is saved from the widget capture surface
- **THEN** the widget updates to reflect the new entry rather than continuing to show stale content

#### Scenario: The displayed entry is deleted

- **WHEN** the entry currently displayed on the widget is deleted
- **THEN** the widget stops displaying it and shows another entry, or the prompt if none remain

#### Scenario: The displayed entry is edited

- **WHEN** the entry currently displayed on the widget has its text changed
- **THEN** the widget shows the updated text rather than the previous text

#### Scenario: All entries are deleted

- **WHEN** the last remaining entry is deleted
- **THEN** the widget reverts to the empty prompt

### Requirement: Adapting to widget size

The widget SHALL remain usable and legible across the sizes a user can resize it to. Because widget space is fixed, a displayed entry's text MAY be truncated to fit, and the capture target SHALL remain reachable at every supported size.

#### Scenario: Resized smaller

- **WHEN** a user resizes the widget to a smaller size
- **THEN** its layout adapts to the new size without content overflowing or being clipped mid-glyph

#### Scenario: A long entry in a small widget

- **WHEN** the displayed entry's text is too long for the available space
- **THEN** the text is truncated with a visible ellipsis rather than overflowing

#### Scenario: Capture stays reachable

- **WHEN** the widget is at its smallest supported size
- **THEN** the capture target is still present and tappable
