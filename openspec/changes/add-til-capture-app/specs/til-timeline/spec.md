## Purpose

The reading side of the app: past entries presented as a browsable card feed, and the controls that narrow that feed down to a stretch of time or a set of labels.

## ADDED Requirements

### Requirement: Staggered card feed

The system SHALL present entries as a multi-column feed of cards whose heights vary with the length of the entry text. A card SHALL show the entry's text wrapped across as many lines as it needs, up to a clamp of six lines, beyond which the text SHALL be truncated with a visible ellipsis and the remainder SHALL be reachable by opening the entry. The clamp SHALL be expressed in lines rather than a fixed height, so that it holds at every system font scale. Each card SHALL carry the entry's capture date, time, and labels at its foot, below the text.

#### Scenario: Card height follows text length

- **WHEN** the feed contains a mix of short and long entries
- **THEN** each card is sized to fit its own text, up to the six-line clamp
- **AND** the resulting columns have cards of differing heights rather than a uniform grid

#### Scenario: A short entry is shown completely

- **WHEN** an entry's text fits within the clamp
- **THEN** its text wraps onto as many lines as it needs and is shown in its entirety
- **AND** no ellipsis is applied

#### Scenario: A long entry is clamped

- **WHEN** an entry's text would exceed the clamp
- **THEN** the card shows the leading lines followed by a visible ellipsis
- **AND** the card does not grow taller to accommodate the remaining text

#### Scenario: The clamp tracks the font scale

- **WHEN** the system font scale is increased
- **THEN** the card still clamps at six lines of the larger text rather than at a fixed pixel height

#### Scenario: Each card is stamped

- **WHEN** a card is displayed
- **THEN** it shows the date and the time of day the entry was captured, at the card's foot
- **AND** it shows the entry's labels, also at the foot, when it has any

#### Scenario: Newest first

- **WHEN** the feed is displayed with no filters applied
- **THEN** entries appear ordered by capture timestamp, most recent first

#### Scenario: A new entry appears immediately

- **WHEN** an entry is saved while the timeline is on screen
- **THEN** it appears at the top of the feed without any manual refresh

### Requirement: Date range filtering

The system SHALL let a user narrow the feed to a period of time using preset ranges — Today, This week, This month, and All — and using a custom start-and-end date range. Preset periods SHALL be evaluated in the device's local timezone, and week boundaries SHALL follow the device locale's first day of week.

#### Scenario: Filtering to today

- **WHEN** a user selects the Today preset
- **THEN** the feed shows only entries whose capture date is the current local date

#### Scenario: Filtering to the current week

- **WHEN** a user selects the This week preset
- **THEN** the feed shows only entries captured in the current week, where the week begins on the device locale's first day of week

#### Scenario: Filtering to the current month

- **WHEN** a user selects the This month preset
- **THEN** the feed shows only entries captured within the current calendar month

#### Scenario: Removing the date filter

- **WHEN** a user selects the All preset
- **THEN** the feed shows entries from every date

#### Scenario: Custom range is inclusive

- **WHEN** a user picks a custom range with a start date and an end date
- **THEN** the feed shows entries captured on the start date, the end date, and every date between them

#### Scenario: Single-day custom range

- **WHEN** a user picks a custom range whose start and end are the same date
- **THEN** the feed shows exactly the entries captured on that date

#### Scenario: Default filter state

- **WHEN** the app is opened from a cold start
- **THEN** the feed opens with All selected and no label filters active

#### Scenario: Filters survive navigation within a session

- **WHEN** a user applies filters, navigates to another screen, and returns to the timeline
- **THEN** the filters they applied are still in effect

### Requirement: Label filtering

The system SHALL let a user narrow the feed by selecting one or more labels. An entry SHALL match when it carries any of the selected labels. Label selection and date range SHALL apply together, so a shown entry must satisfy both.

#### Scenario: Filtering by one label

- **WHEN** a user selects a single label
- **THEN** the feed shows only entries carrying that label

#### Scenario: Filtering by several labels

- **WHEN** a user selects two or more labels
- **THEN** the feed shows every entry carrying at least one of the selected labels

#### Scenario: Label and date filters combine

- **WHEN** a user has both a date range and one or more labels selected
- **THEN** the feed shows only entries that fall inside the date range and carry at least one selected label

#### Scenario: Only labels in use are offered

- **WHEN** the label filter options are displayed
- **THEN** they include every label attached to at least one existing entry and no others

#### Scenario: Deselecting a label

- **WHEN** a user deselects a previously selected label
- **THEN** the feed widens to include entries that were excluded by that selection

### Requirement: Distinguishable empty states

The system SHALL distinguish having no entries at all from having no entries that match the active filters, and SHALL present a different message for each. When filters are responsible for the empty result, the system SHALL offer a way to clear them.

#### Scenario: No entries have ever been captured

- **WHEN** the timeline is opened and no entries exist
- **THEN** an invitation to capture a first entry is shown instead of an empty feed

#### Scenario: Filters exclude everything

- **WHEN** the active filters match no entries but entries do exist
- **THEN** a message stating that nothing matches the current filters is shown
- **AND** an action to clear the filters is offered

#### Scenario: Clearing filters from the empty state

- **WHEN** a user activates the clear-filters action
- **THEN** the date range returns to All, all label selections are dropped, and the full feed is shown

### Requirement: Acting on an entry from the feed

The system SHALL let a user reach the edit and delete actions for an entry directly from its card in the feed.

#### Scenario: Opening an entry to edit it

- **WHEN** a user taps an entry's card
- **THEN** that entry opens for editing with its current text and labels populated
- **AND** the text is shown in full, without the feed's six-line clamp or ellipsis

#### Scenario: Deleting from the feed

- **WHEN** a user invokes the delete action on a card
- **THEN** the entry is deleted and the undo affordance is offered
- **AND** the feed closes the gap left by the removed card
