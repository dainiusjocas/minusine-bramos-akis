# Minusinė Bramos akis

![](https://github.com/dainiusjocas/minusine-bramos-akis/workflows/Linting%20and%20Unit%20Tests/badge.svg)

<img src="doc/goldman-sachs.jpg"
 alt="Goldman Sachs but not Like Hillary" title="Goldman Sachs but not Like Hillary"
 align="right" />

> "'Minusinė Bramos akis' - pusiau sąmoningas randominis neuronų tinklas, suteikiantis ribotą prieigą prie visų praeities įvykių, palikusių elektroninį arba šviesos atspaudą, net jeigu tas atspaudas jau sunaikintas."
> - Viktor Pelevin, "iPhuck 10", 2017, 207-208pp"

## Lambda Usage

```bash
curl -s -X POST https://t39kq6o310.execute-api.eu-central-1.amazonaws.com/Prod/observe -d '
{
  "dictionary": [{"text":"Karbauskis"}],
  "search": {
    "url": "delfi.lt/news/.*","from":"201802","to":"201803"
 }
}
' | json_pp
=>
[
   {
      "hits" : [
         {
            "dict-entry-id" : "0",
            "text" : "Karbauskis",
            "meta" : {},
            "end-offset" : 4408,
            "type" : "PHRASE",
            "begin-offset" : 4398
         }
      ],
      "original" : "https://www.delfi.lt/news/daily/crime/alytaus-policija-iesko-nuo-bausmes-besislapstancio-vyro.d?id=77416031",
      "archive-url" : "http://web.archive.org/web/20180314121007/https://www.delfi.lt/news/daily/crime/alytaus-policija-iesko-nuo-bausmes-besislapstancio-vyro.d?id=77416031"
   },
   {
      "hits" : [
         {
            "meta" : {},
            "end-offset" : 3777,
            "type" : "PHRASE",
            "begin-offset" : 3767,
            "text" : "Karbauskis",
            "dict-entry-id" : "0"
         }
      ],
      "original" : "https://www.delfi.lt/news/daily/crime/atsauke-pavoju-sostines-prekybos-centre.d?id=77264831",
      "archive-url" : "http://web.archive.org/web/20180314032345/https://www.delfi.lt/news/daily/crime/atsauke-pavoju-sostines-prekybos-centre.d?id=77264831"
   }
]
```

## License

Copyright &copy; 2019 [Dainius Jocas](https://www.jocas.lt).

Distributed under the The Apache License, Version 2.0.
