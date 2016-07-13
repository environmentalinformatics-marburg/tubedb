#!/bin/bash
#
# This script creates a backup-copy of TubeDB.
# Backup is stored at [current directory]/backup_cron/[timestamp]
#
# (This script is called by cronjob_internal.sh)

#backup root directory
root=backup_cron

#create name for backup directory
target=$root/$(date +%Y_%m_%d___%H_%M__%S)

#create backup root directory if not exist
mkdir --parents backup_cron

#create backup directory
mkdir $target

echo begin backup in $target ...

#check if last command returned error and print warning
function check_warn() {
    if [ $2 -ne 0 ]; then
        echo WARNING $1   $2
    fi
    return $2
}

#copy directory recursive
function copy_directory(){
	#echo copy $1 to $target
	cp --recursive --preserve=all $1 $target
	check_warn "could not copy directory $1" $?
}

#copy file in directory
#syntax: copy_file [directory] [file]
function copy_file(){
	#echo copy $1 to $target
	mkdir $target/$1
	cp --preserve=all $1/$2 $target/$1
	check_warn "could not copy file $1/$2" $?
}

#copy files in directory (not recursive)
function copy_files(){
	#copy files in base directory
	    #cp --preserve=all $1/* $target
	find -maxdepth 1 -type f -exec cp --preserve=all {} $target \;
	check_warn "could not copy files $1" $?
}

#specification of files and directories to copy
copy_directory config
copy_directory lib
copy_directory log
copy_directory storage
copy_directory webcontent
copy_file webFiles info.html
copy_files

echo ... end backup in $target

