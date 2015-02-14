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

The following keyboard shortcuts are supported.

Shortcut            | Action
---                 | ---
`e`                 | Edit page
`a`                 | Add nested page
`d`                 | Delete page
`1` till `9`        | Open `n`th nested page
`Left Arrow`        | Go to the parent page
`Escape`            | Close editing mask or print page
`0`                 | Go to the root page
`Ctrl+s` / `Cmd+s`  | Save

## More

Use [GitHub](https://github.com/chmllr/WikiZen) to open an issue or contribute to WikiZen.
