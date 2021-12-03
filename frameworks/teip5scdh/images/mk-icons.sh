#!/bin/bash
name=${1%.*}

function resize {
	size=$1
	convert $name.png -resize ${size}x${size} $name-${size}.png
}

resize 16
resize 24
