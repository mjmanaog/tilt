## Purpose

The record itself: how a thing-you-learned gets written down, corrected, removed, and kept safe on the device. Every other capability reads what this one writes.

## ADDED Requirements

### Requirement: Capture a TIL entry

The system SHALL let a user record a TIL entry consisting of required free text, zero or more optional labels, and a capture timestamp assigned automatically by the system. The user SHALL NOT be asked to supply the timestamp.

#### Scenario: Saving an entry with text only

- **WHEN** a user submits an entry whose text contains at least one non-whitespace character
- **THEN** the entry is persisted with the current local date and time as its capture timestamp
- **AND** the entry becomes visible in the timeline without any manual refresh

#### Scenario: Saving an entry with labels

- **WHEN** a user submits an entry with text and one or more labels attached
- **THEN** the entry is persisted with all of those labels associated with it
- **AND** the entry can subsequently be found by filtering on any one of those labels

#### Scenario: Blank text is rejected

- **WHEN** a user attempts to save an entry whose text is empty or contains only whitespace
- **THEN** the save action is unavailable and no entry is created

#### Scenario: Surrounding whitespace is discarded

- **WHEN** a user submits entry text with leading or trailing whitespace or newlines
- **THEN** the persisted text has that surrounding whitespace removed
- **AND** whitespace and line breaks inside the text are preserved as typed

#### Scenario: Long entries are stored in full

- **WHEN** a user submits an entry of several hundred characters
- **THEN** the entire text is persisted and later returned without truncation

### Requirement: Free-form labels

The system SHALL accept labels as free-form text typed by the user, with no fixed vocabulary. Labels SHALL be normalized so that entries differing only by surrounding whitespace or letter case resolve to the same label, and the label set offered for filtering SHALL contain only labels currently attached to at least one entry.

#### Scenario: Labels differing only by case are the same label

- **WHEN** a user attaches the label `Science` to one entry and `science` to another
- **THEN** both entries are treated as carrying the same label
- **AND** filtering on that label returns both entries

#### Scenario: Duplicate label on one entry is stored once

- **WHEN** a user adds the same label twice to a single entry
- **THEN** the label is associated with that entry exactly once

#### Scenario: Previously used labels are suggested

- **WHEN** a user begins typing in the label field and the typed characters are a prefix of one or more previously used labels
- **THEN** those matching labels are offered for selection
- **AND** selecting one attaches it without the user finishing typing

#### Scenario: A brand-new label is accepted

- **WHEN** a user types a label that has never been used before and confirms it
- **THEN** the label is created and attached to the entry
- **AND** it becomes available as a suggestion and as a filter option

#### Scenario: An unused label disappears from filters

- **WHEN** the last entry carrying a given label is deleted, or that label is removed from the last entry carrying it
- **THEN** the label no longer appears among the available filter options

#### Scenario: An entry may carry no labels

- **WHEN** a user saves an entry without attaching any label
- **THEN** the entry is persisted successfully with an empty label set

### Requirement: Edit an existing entry

The system SHALL let a user change the text and the labels of an already-saved entry. Editing SHALL NOT change the entry's original capture timestamp.

#### Scenario: Editing entry text

- **WHEN** a user opens a saved entry, changes its text, and confirms
- **THEN** the entry shows the new text everywhere it appears
- **AND** its displayed date and time remain those of the original capture

#### Scenario: Editing entry labels

- **WHEN** a user adds or removes labels on a saved entry and confirms
- **THEN** the entry's label set reflects the change
- **AND** label filtering matches the entry against its new label set

#### Scenario: Editing cannot blank an entry

- **WHEN** a user clears all text from a saved entry and attempts to confirm
- **THEN** the save action is unavailable and the entry retains its previous text

#### Scenario: Abandoning an edit changes nothing

- **WHEN** a user modifies an entry and leaves without confirming
- **THEN** the entry retains its previously saved text and labels

### Requirement: Delete an entry with undo

The system SHALL let a user delete an entry, and SHALL offer a time-limited opportunity to reverse that deletion. Once the opportunity lapses the deletion SHALL be permanent.

#### Scenario: Deleting an entry

- **WHEN** a user deletes an entry
- **THEN** the entry is removed from the timeline and from all statistics
- **AND** an undo affordance is presented

#### Scenario: Undoing a deletion

- **WHEN** a user activates the undo affordance before it lapses
- **THEN** the entry is restored with its original text, labels, and capture timestamp
- **AND** it returns to its original position in the timeline ordering

#### Scenario: Deletion becomes permanent

- **WHEN** the undo affordance lapses or is dismissed without being activated
- **THEN** the entry is permanently gone and is absent after the app is restarted

### Requirement: Durable on-device storage

Entries SHALL persist across app restarts and device reboots. All entry data SHALL remain on the device: the system SHALL NOT transmit entry text, labels, or timestamps to any network destination, and SHALL require no account or sign-in.

#### Scenario: Entries survive a restart

- **WHEN** the app is force-stopped and relaunched
- **THEN** every previously saved entry is present with its text, labels, and capture timestamp intact

#### Scenario: Full function without a network

- **WHEN** the device has no network connectivity
- **THEN** capturing, browsing, editing, deleting, and viewing statistics all work normally

#### Scenario: No sign-in is required

- **WHEN** a user opens the app for the first time
- **THEN** they can capture an entry immediately without creating an account or signing in
