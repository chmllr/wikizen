WikiZen
=======

           _..oo8"""Y8b.._
         .88888888o.    "Yb.
       .d888P""Y8888b      "b.
      o88888    88888)       "b
     d888888b..d8888P         'b
     88888888888888"           8
    (88DWB8888888P             8)
     8888888888P               8
     Y88888888P     ee        .P
      Y888888(     8888      oP
       "Y88888b     ""     oP"
         "Y8888o._     _.oP"
           `""Y888boodP""'

> "Like all magnificent things,  
>  it's very simple."  
> — _Natalie Babbitt_

WikiZen is a minimal Markdown-based wiki engine for simple Wikis.
WikiZen serializes a Wiki into one JSON object, which makes it storable **anywhere**.

## Specification

### Wiki and Wiki Pages

A __Wiki__ is one single JSON object, containing all the settings and __Wiki Pages__.
In terms of abstract types, we can describe Wikie and Wiki Pages as follows.

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
                
This simple Wiki has a main Wiki Page, which has one child.
Obviously, a Wiki is wrapper for Wiki Pages represented as a simple _ordered_ tree data structure.

### References

Remember, a Wiki Page is just a node of a tree.
This node can have arbitrarily many child nodes.
Child nodes are stored in an _ordered_ list.

Hence, we can identify any node in this tree by a unique sequence of indices, which we call a _reference_.
A reference denotes a "path": each step of this path is the number of a child we have to open, starting from the root.

That is, for any given Wiki Page `p` (it doesn't have to be the root, obviously), a reference `[]` returns `p` itself.
Since the sequence is empty, we do not have to open any child notes.
A sequence `[i]` references `i`th child of `p`.
A sequence `[i j]` references `j`th child of `p`'s `i`'th child.

### Update Deltas

WikiZen handles Wikis as immutable data structures. All updates are stored separately as deltas.
When a Wiki is loaded, it's assembled from the stored deltas.

A delta is defines as a triple:

    Delta = {
        ref: [Int], 
        property: String,
        delta: String
    }
    
Reference identifies the Wiki Page.
Property identifies the object property like `title` or `body`.
Delta is diff (produced by Diff-Match-Path library), containing the deltas only.


