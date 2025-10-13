#! /bin/bash
# Processes al files in doc/diagrams 
# We generate svg files.

for f in *.dot
do
	if [ -f "$f" ]
	then
		echo "$f"
		basename=`basename $f .dot`
		dot -Tsvg $f > "$basename.svg"
	fi
done

java -jar $PLANTUML_JAR "*.puml" -tsvg


# uncomment next lines if there are msc files
#for f in *.msc
#do
#    echo ":$f:"
#    basename=`basename $f .msc`
#    mscgen -Tsvg $f > "$basename.svg"
#done

