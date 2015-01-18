# Specification of the Serialization Format

## Wiki and Wiki Pages

A __Wiki__ is one single JSON object, containing settings and __Wiki Pages__.
In terms of abstract types, we can describe a Wiki and Wiki Pages as follows.

    Wiki = {
      name: String,
      freeID: Int,
      root: WikiPage,
      deltas: [Delta]
    }

A wiki object has a name, next free page ID, root page and a list of deltas, representing a list of changes.
At run time, this object is rendered to a tree of wiki pages of the following type:

    WikiPage = {
      id: Int,
      title: String,
      body: String,
      children: [WikiPage]
    }

## IDs

Every page has a unique ID used to identify this page in the Wiki tree.
The Wiki object holds the next free ID used for adding of new pages.
After a new page is added, the id will be incremented.

## Update Deltas

WikiZen handles Wikis as immutable data structures. Every update is stored separately as a delta.
When a Wiki is loaded, it's assembled from the stored deltas.

A delta is defined as a triple:

    Delta = {
      timestamp: Long,
      pageID: Int,
      property: String,
      value: Object
    }

`timestamp` the UNIX timestamp denoting the creation date of the delta.
`pageID` identifies the page to be changed.
`property` specifies the property to be changed. Following properties are : `title`, `body` and `page`.
`value` is different for every property:
  - for `title` it is the new title;
  - for `body` it is a diff (produced by Diff-Match-Path library), containing the deltas only;
  - for `page` it can be an arbitrary JSON object (interpreted as a child) or
  `null` (if the page was deleted).