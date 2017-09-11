
index.html:readme.md
	pandoc \
	--smart \
	--table-of-contents \
	--css=http://b.enjam.info/panam/styling.css -V lang=en -V highlighting-css= \
	--to=html5 readme.md -o index.html