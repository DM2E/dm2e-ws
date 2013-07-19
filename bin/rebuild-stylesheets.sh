#!/bin/bash

listOfLESS=(
WebContent/css/theme-dark
WebContent/css/theme-light
);
for i in ${listOfLESS[@]};do
    lessc ${i}.less > ${i}.css;
done
