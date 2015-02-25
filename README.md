# README

> "Simplicity is prerequisite for reliability."

> — _Dijkstra_

WikiZen is a minimalistic Markdown-based wiki.
WikiZen serializes to a single JSON object, which makes it storable **anywhere**.
See the [technical specification](https://github.com/chmllr/WikiZen/blob/master/SPEC.md) of the persistence format.

## How to use

### Writing

Use [Markdown language](http://en.wikipedia.org/wiki/Markdown) or <mark>standard HTML</mark> tags.
Every page can contain nested pages.
Pages can be [easily linked](#page=1) using Markdown syntax.

### Navigation

<<<<<<< HEAD
Following shortcuts are supported:
- `e`: edit current page;
- `a`: add a nested page;
- `d`: delete current page;
- `1` till `9`: open the `n`th nested page;
- `Left Arrow`: go to the parent page;
- `Escape`: close the editing mask;
- `0`: go to the root page;
- `Meta + s`: saves the changes.
=======
The following keyboard shortcuts are supported.

Shortcut                    | Action
---                         | ---
`e`                         | Edit page
`a`                         | Add nested page
`d`                         | Delete page
`1` - `9`                   | Go to the `n`th nested page
`0`                         | Go to the root page
`Left Arrow`                | Go to the parent page
`f`                         | Set focus to search field
`Alt` + `1` - `9`    | Open `n`th search result
`Esc`                       | Close editing mask or print page
`Ctrl / Cmd + s`            | Save
>>>>>>> 1a9e99f55fa7e6c370d3e338d76fc8d6fbb7f995

## More

Use [GitHub](https://github.com/chmllr/WikiZen) to open an issue or contribute to WikiZen.
