# &#9775; WikiZen

> "Simplicity is prerequisite for reliability."

> — _Dijkstra_

WikiZen is a minimalistic Markdown-based wiki.
WikiZen serializes to a single JSON object, which makes it storable **anywhere**.
See the [technical specification](https://github.com/chmllr/WikiZen/blob/master/SPEC.md) of the persistence format.

## How to use

### Writing

Use [Markdown markup language](http://en.wikipedia.org/wiki/Markdown).
On every page nested pages can be added.
Pages can be [easily linked](#page=1) using Markdown syntax.

### Navigation

Following shortcuts are supported:
- `e`: edit current page;
- `a`: add a nested page;
- `d`: delete current page;
- `1` till `9`: open the `n`th nested page;
- `Left Arrow`: go to the parent page;
- `Escape`; close the editing mask;
- `0`: go to the root page.

## More

Use [GitHub](https://github.com/chmllr/WikiZen) to open an issue or contribute to WikiZen.