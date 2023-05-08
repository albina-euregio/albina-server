# albina-server

The server stores and processes all relevant information for the ALBINA system such as bulletins.

## Bulletin status/workflow

```mermaid
flowchart TD
    missing --> |edit| draft
    draft --> |submit| submitted
    submitted --> |edit| draft
    submitted --> |publish (automated publication at 5PM)| published
    published --> |edit| updated
    updated --> |submit| resubmitted
    resubmitted --> |edit| updated
    resubmitted --> |publish (automated update at 8AM or manually at any time)| republished
    republished --> |edit| updated
```
