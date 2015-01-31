# Markdown Features

## Text Formatting

[Markdown](http://en.wikipedia.org/wiki/Markdown) is a _markup language_ with plain text
formatting syntax designed so that it can be converted to **HTML and many other formats** using a tool by the same name.

## Backquotes, Lists & Code

This is a backquote:

> _"Our greatest glory is not in never falling but in rising every time we fall."_
> — Confucius

To create simple lists, just enumerate all items using a dash in the prefix:

- Alpha
- Beta
- Gamma

Also you can either mark some special `words` or write entire `code` blocks:

    (defn fact [n]
      (if (< n 2) 1
        (* n (fact (- n 1)))))

## Tables

Also simple tables is a piece of cake:

Parameter    | Explanation                              | Type
---          | ---                                      | ---
`note`       | Text to publish                          | **required**
`password`   | Secret token (plain or hashed)           | *optional*