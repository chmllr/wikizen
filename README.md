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

    Wiki = { name: String, root: WikiPage}
    
    WikiPage = {
      title: String,
      body: String,
      children: [WikiPage]
    }

Here is an example of a valid Wiki JSON object:

    {
      "name": "Demo Wiki",
      "root": { 
        "title": "Main Page",
        "body": "Hello *world*!",
                "children" [
                  {
                    "title": "Child Page",
                    "body": "Hello _back_!" 
                  }
               ]
        }
    }
                
This simple Wiki has a root Wiki Page, which has one child.
Obviously, a Wiki is wrapper for Wiki Pages represented as a simple _ordered_ tree data structure.

#### References

Remember, a Wiki Page is just a node of a tree.
This node can have arbitrarily many child nodes.
Child nodes are stored in an _ordered_ list.

Hence, we can identify any node in this tree by a unique sequence of indices, which we call a _reference_.
A reference denotes a "path": each step of this path is the number of a child we have to open, starting from the root.

That is, for any given Wiki Page `p` (it doesn't have to be the root, obviously), a reference `[]` returns `p` itself:
since the sequence is empty, we do not have to open any child notes.
A sequence `[i]` references `i`th child of `p`.
A sequence `[i j]` references `j`th child of `p`'s `i`'th child.

#### Update Deltas

WikiZen handles Wikis as immutable data structures. All updates are stored separately as deltas.
When a Wiki is loaded, it's assembled from the stored deltas.

A delta is defined as a triple:

    Delta = {
      ref: [Int], 
      property: String,
      delta: String
    }
    
`ref` identifies the Wiki Page to be changed.
`property` identifies the object property like `title`, `body` or `page`.
`delta` is different for every property:
  - for title it is the new title;
  - for body it is a diff (produced by Diff-Match-Path library), containing the deltas only;
  - for page it can be an arbitrary JSON object or even `null` (it the page was deleted).
  
### Serialization

A Wiki is stored in one single JSON object, whose structure can be described in the same type abstraction used above as follows:

    {
      root: Wiki,
      deltas: [Delta]
    }

