#!/bin/bash
#
# Copyright 2016 agido GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#	 http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

if [ "$(uname)" = "Linux" ]; then
	ARCH=linux
elif [ "$(uname)" = "Darwin" ]; then
	ARCH=osx
	alias find='gfind'
	alias sed='gsed'
	alias awk='gawk'
else
	echo "Unsupported OS '$(uname)'"
	exit 1
fi

function _md5sum() {
	local file=$1
	if [ $ARCH = "linux" ]; then
		md5sum $file 2>/dev/zero | cut -d ' ' -f 1
	else
		md5 $file 2>/dev/zero | cut -d '=' -f 2 | cut -b 2-
	fi
}

function download() {
	local file=$1
	local url=$2
	local sum=$3

	if [ "$(_md5sum $file)" != "$sum" ]; then
		rm -f $file $file.tmp || true
		echo "Downloading $file"
		if curl -sS -L $url -o $file.tmp ; then
			mv $file.tmp $file
		else
			exit 1
		fi
	fi

	local checksum=$(_md5sum $file)
	if [ "$checksum" != "$sum" ]; then
		echo "Downloading $file failed! checksum is $checksum, expected was $sum"
		exit 1
	fi
}

function unpack() {
	local from=$1
	local what=$2
	local file=$3
	local sum=$4

	if [ "$(_md5sum $file)" != "$sum" ]; then
		if [[ "$from" == *.zip ]]; then
			unzip $from $what -d $(dirname $file)
		else
			rm -f $file || true
			tar xf $from --transform "s:$what:$file:" $what
		fi
	fi
	local checksum=$(_md5sum $file)
	if [ "$checksum" != "$sum" ]; then
		rm -f $file || true
		echo "unpacking $file failed! checksum is $checksum, expected was $sum"
		exit 1
	fi
}

function setup_vnc() {
	if [ $ARCH = "linux" ]; then
		local tigervnc=tigervnc-1.8.0.x86_64.tar.gz
		download vnc/$tigervnc https://bintray.com/tigervnc/stable/download_file?file_path=$tigervnc 4493f1acbea0ba11f88ffa485764ce25
		unpack vnc/$tigervnc tigervnc-1.8.0.x86_64/usr/bin/Xvnc vnc/Xvnc 4d0d76507a65399a7587a75a68cffcde
		unpack vnc/$tigervnc tigervnc-1.8.0.x86_64/usr/bin/vncviewer vnc/vncviewer 2ccf2c3b5c526f8d0c814da8ea73f8d4
	else
		echo "skipping.. vnc currently not tested on OSX!"
	fi
}

function setup_seleniumserver() {
	download selenium/selenium-server-standalone.jar https://selenium-release.storage.googleapis.com/3.9/selenium-server-standalone-3.9.0.jar 306a475b6f1d540969416cdf11cd7361
}

function setup_chromedriver() {
	if [ $ARCH = "linux" ]; then
		download selenium/chromedriver_linux64.zip https://chromedriver.storage.googleapis.com/2.35/chromedriver_linux64.zip e6d0298d3e1ed23f6639805d13ac2ae4
		unpack selenium/chromedriver_linux64.zip chromedriver selenium/chromedriver 374c611c3b5362e2323d2387dd470d15
		if [ ! -e selenium/chromedriver_linux ]; then
		    ln -s chromedriver selenium/chromedriver_linux
		fi
	else
		echo "skipping.. chromedriver currently not tested on OSX!"
	fi
}

function setup_geckodriver() {
	if [ $ARCH = "linux" ]; then
		download selenium/geckodriver_linux64.tgz https://github.com/mozilla/geckodriver/releases/download/v0.19.1/geckodriver-v0.19.1-linux64.tar.gz ad21bc63be0bae119416755ea7c44768
		unpack selenium/geckodriver_linux64.tgz geckodriver selenium/geckodriver cf2ed8b2f014ca3e68b5c2dc4607ec20
		if [ ! -e selenium/geckodriver_linux ]; then
		    ln -s geckodriver selenium/geckodriver_linux
		fi
	else
		echo "skipping.. geckodriver currently not tested on OSX!"
	fi
}

function setup() {
	setup_vnc
	setup_seleniumserver
	setup_geckodriver
	setup_chromedriver
}

setup
