&#9775; WikiZen
=======

> "Simplicity is prerequisite for reliability."
> — _Dijkstra_

WikiZen is a minimal Markdown-based wiki engine for simple Wikis.
WikiZen serializes a Wiki into one JSON object, which makes it storable **anywhere**.

## Usability

### Shortcuts

Following shortcuts are supported:

- `e` opens the current page in editing mode;
- `n` opens the new-page mask;
- `d` deletes the current page;
- `1` till `9` opens the `n`th child of the current page;
- `Left Arrow` navigates to previous page.

## Development

### Specification

#### Wiki and Wiki Pages

A __Wiki__ is one single JSON object, containing settings and __Wiki Pages__.
In terms of abstract types, we can describe a Wiki and Wiki Pages as follows.

    Wiki = {
      name: String,
      root: WikiPage,
      deltas: [Delta]
    }
    
    WikiPage = {
      id: Int,
      title: String,
      body: String,
      children: [WikiPage]
    }

This simple Wiki has a root Wiki Page, which has one child.
Obviously, a Wiki is wrapper for Wiki Pages represented as a simple _ordered_ tree data structure.

#### IDs

Every page has a unique ID used to identify this page in the Wiki tree.
The Wiki object holds the next free id used for adding of new pages.
After a new page is added, the id will be incremented.

#### Update Deltas

WikiZen handles Wikis as immutable data structures. All updates are stored separately as deltas.
When a Wiki is loaded, it's assembled from the stored deltas.

A delta is defined as a triple:

    Delta = {
      pageID: Int,
      property: String,
      value: Object
    }
    
`pageID` identifies the page to be changed.
`property` specifies the property to be changed. Following properties are : `title`, `body` and `page`.
`value` is different for every property:
  - for `title` it is the new title;
  - for `body` it is a diff (produced by Diff-Match-Path library), containing the deltas only;
  - for `page` it can be an arbitrary JSON object (interpreted as a child) or
  `null` (if the page was deleted).
  
### Serialization

A Wiki is stored in one single JSON object, whose structure can be described in the same type abstraction used above as follows:

    {
      root: Wiki,
      deltas: [Delta]
    }

